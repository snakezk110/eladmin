/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.modules.security.service.OnlineUserService;
import me.zhengjie.modules.security.service.UserCacheClean;
import me.zhengjie.modules.system.domain.*;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.dto.*;
import me.zhengjie.modules.system.service.mapstruct.UserMapper;
import me.zhengjie.utils.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileProperties properties;
    private final RedisUtils redisUtils;
    private final UserCacheClean userCacheClean;
    private final OnlineUserService onlineUserService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Object queryAll(UserQueryCriteria criteria, Pageable pageable) {
        Page<User> page = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(userMapper::toDto));
    }

    @Override
    public List<UserDto> queryAll(UserQueryCriteria criteria) {
        List<User> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        return userMapper.toDto(users);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public UserDto findById(long id) {
        User user = userRepository.findById(id).orElseGet(User::new);
        ValidationUtil.isNull(user.getId(), "User", "id", id);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(User resources) {
        if (userRepository.findByUsername(resources.getUsername()) != null) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }
        if (userRepository.findByEmail(resources.getEmail()) != null) {
            throw new EntityExistException(User.class, "email", resources.getEmail());
        }
        userRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User resources) {
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        ValidationUtil.isNull(user.getId(), "User", "id", resources.getId());
        User user1 = userRepository.findByUsername(resources.getUsername());
        User user2 = userRepository.findByPhone(resources.getPhone());

        if (user1 != null && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }

        if (user2 != null && !user.getId().equals(user2.getId())) {
            throw new EntityExistException(User.class, "Phone", resources.getPhone());
        }
        // 如果用户的角色改变
        if (!resources.getRoles().equals(user.getRoles())) {
            redisUtils.del(CacheKey.DATE_USER + resources.getId());
            redisUtils.del(CacheKey.MENU_USER + resources.getId());
            redisUtils.del(CacheKey.ROLE_AUTH + resources.getId());
        }
        // 如果用户名称修改
        if(!resources.getUsername().equals(user.getUsername())){
            redisUtils.del("user::username:" + user.getUsername());
        }
        // 如果用户被禁用，则清除用户登录信息
        if(!resources.getEnabled()){
            onlineUserService.kickOutForUsername(resources.getUsername());
        }
        user.setUsername(resources.getUsername());
        user.setEmail(resources.getEmail());
        user.setEnabled(resources.getEnabled());
        user.setRoles(resources.getRoles());
        user.setDept(resources.getDept());
        user.setJobs(resources.getJobs());
        user.setPhone(resources.getPhone());
        user.setNickName(resources.getNickName());
        user.setGender(resources.getGender());
        userRepository.save(user);
        // 清除缓存
        delCaches(user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCenter(User resources) {
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        user.setNickName(resources.getNickName());
        user.setPhone(resources.getPhone());
        user.setGender(resources.getGender());
        userRepository.save(user);
        // 清理缓存
        delCaches(user.getId(), user.getUsername());
    }

    @Override
    public Object getPhoneCode(String phone,String verifyCode) {
        Map<String, Object> respMap = new HashMap<>();
        respMap.put("status", -1);
        respMap.put("message", "此功能暂时关闭！");
//        String phoneCode = (String) redisUtils.get(CacheKey.USER_PHONE+phone);
//        try {
//            if ( StringUtils.isNotBlank(phoneCode)) {
//                respMap.put("message", "您已经获取验证码！");
//                return respMap;
//            }
//            String strMD5=DigestUtils.md5Hex(phone+"LP!kYpgayWtd6j6%");
//            if(!strMD5.equalsIgnoreCase(verifyCode)){
//                respMap.put("message", "非法请求！");
//                return respMap;
//            }
//            /* 必要步骤：
//             * 实例化一个认证对象，入参需要传入腾讯云账户密钥对secretId，secretKey。
//             * 这里采用的是从环境变量读取的方式，需要在环境变量中先设置这两个值。
//             * 你也可以直接在代码中写死密钥对，但是小心不要将代码复制、上传或者分享给他人，
//             * 以免泄露密钥对危及你的财产安全。
//             * CAM密匙查询: https://console.cloud.tencent.com/cam/capi*/
//            Credential cred = new Credential("AKIDUlmWzCtYAm8ZUZ4nMfspVu5i60e1ozZs", "A5nGSaHA4j8E3arviETVljPsnEOIwJUN");
//
//            /* 非必要步骤:
//             * 实例化一个客户端配置对象，可以指定超时时间等配置 */
//            ClientProfile clientProfile = new ClientProfile();
//            /* SDK默认用TC3-HMAC-SHA256进行签名
//             * 非必要请不要修改这个字段 */
//            clientProfile.setSignMethod("HmacSHA256");
////            clientProfile.setHttpProfile(httpProfile);
//            /* 实例化要请求产品(以sms为例)的client对象
//             * 第二个参数是地域信息，可以直接填写字符串ap-guangzhou，或者引用预设的常量 */
//            SmsClient client = new SmsClient(cred, "ap-guangzhou", clientProfile);
//            /* 实例化一个请求对象，根据调用的接口和实际情况，可以进一步设置请求参数
//             * 你可以直接查询SDK源码确定接口有哪些属性可以设置
//             * 属性可能是基本类型，也可能引用了另一个数据结构
//             * 推荐使用IDE进行开发，可以方便的跳转查阅各个接口和数据结构的文档说明 */
//            SendSmsRequest req = new SendSmsRequest();
//
//            /* 填充请求参数,这里request对象的成员变量即对应接口的入参
//             * 你可以通过官网接口文档或跳转到request对象的定义处查看请求参数的定义
//             * 基本类型的设置:
//             * 帮助链接：
//             * 短信控制台: https://console.cloud.tencent.com/sms/smslist
//             * sms helper: https://cloud.tencent.com/document/product/382/3773 */
//
//            /* 短信应用ID: 短信SdkAppid在 [短信控制台] 添加应用后生成的实际SdkAppid，示例如1400006666 */
//            String appid = "1400412483";
//            req.setSmsSdkAppid(appid);
//
//            /* 短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名，签名信息可登录 [短信控制台] 查看 */
//            String sign = "亚成电子";
//            req.setSign(sign);
//
//            /* 国际/港澳台短信 senderid: 国内短信填空，默认未开通，如需开通请联系 [sms helper] */
//            String senderid = "";
//            req.setSenderId(senderid);
//
//            /* 用户的 session 内容: 可以携带用户侧 ID 等上下文信息，server 会原样返回 */
//            String session = "test";
//            req.setSessionContext(session);
//
//            /* 短信码号扩展号: 默认未开通，如需开通请联系 [sms helper] */
////            String extendcode = "xxx";
////            req.setExtendCode(extendcode);
//            /* 模板 ID: 必须填写已审核通过的模板 ID。模板ID可登录 [短信控制台] 查看 */
//            String templateID = "691758";
//            req.setTemplateID(templateID);
//            /* 下发手机号码，采用 e.164 标准，+[国家或地区码][手机号]
//             * 示例如：+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号*/
//            String[] phoneNumbers = {"+86" + phone};
//            req.setPhoneNumberSet(phoneNumbers);
//            String code =  String.valueOf((int)((Math.random()*9+1)*100000));
//            redisUtils.set(CacheKey.USER_PHONE+phone,code,600);
//            /* 模板参数: 若无模板参数，则设置为空*/
//            String[] templateParams = {code, "10"};
//            req.setTemplateParamSet(templateParams);
//            /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
//             * 返回的 res 是一个 SendSmsResponse 类的实例，与请求对象对应 */
//            SendSmsResponse res = client.SendSms(req);
//            // 输出json格式的字符串回包
//            //System.out.println(SendSmsResponse.toJsonString(res));
//            respMap.put("status", HttpStatus.OK.value());
//            //respMap.put("message", "waiting for mobile interface complete! but you can test it user  the code 123456");
//            respMap.put("message", "发送短信成功！");
            return respMap;
            //return SendSmsResponse.toJsonString(res);
            // 也可以取出单个值，你可以通过官网接口文档或跳转到response对象的定义处查看返回字段的定义
            //System.out.println(res.getRequestId());
//        } catch (TencentCloudSDKException e) {
//            redisUtils.del(CacheKey.USER_PHONE+phone);
//            respMap.put("message", e.toString());
//            return respMap;
//        }
    }

    @Override
    public Object firstLogin(UserReq userReq) {
        Map<String, Object> respMap = new HashMap<>();
       // String strCode = (String) redisUtils.get("user::phone"+userReq.getPhone());
        respMap.put("status", -1);
        respMap.put("message", "此功能暂时关闭！");
//        if (StringUtils.isBlank(strCode) ) {
//            respMap.put("message", "手机验证码不能为空！");
//            return respMap;
//        }
//
//        if (!userReq.getPhoneCode().equalsIgnoreCase(strCode)) {
//            respMap.put("message", "手机验证码不正确!");
//            return respMap;
//        }
//
//        User user = new User();
//        user.setPhone(userReq.getPhone());
//        user.setIsAdmin(false);
//        user.setEnabled(true);
//        Dept dept=new Dept();
//        dept.setId(16L);
//        user.setDept(dept);
//        user.setPassword(userReq.getPassword());
//        user.setEmail("test@163.com");
//        user.setNickName(userReq.getNickName());
//        user.setUsername(userReq.getUsername());
//        user.setPassword(passwordEncoder.encode(userReq.getPassword()));
//        SysSchool sysSchool=new SysSchool();
//        sysSchool.setSchoolId(userReq.getSchool_id());
//        user.setSchool(sysSchool);
//        Set<Job> jobs=new HashSet<>() ;
//        Job job=new Job();
//        job.setId(13L);
//        jobs.add(job);
//        user.setJobs(jobs);
//        Set<Role> roles=new HashSet<>() ;
//        Role role=new Role();
//        role.setId(3L);
//        roles.add(role);
//        user.setRoles(roles);
//        try {
//            userRepository.save(user);
//        }catch (Exception e){
//            respMap.put("message", "手机号码或者用户名已经存在！");
//            e.getMessage();
//            return respMap;
//        }
//        //删除手机验证码;
//        redisUtils.del("user::phone"+user.getPhone());
//        respMap.put("status", HttpStatus.OK.value());
//        respMap.put("message", "注册成功！");
        return respMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            // 清理缓存
            UserDto user = findById(id);
            delCaches(user.getId(), user.getUsername());
        }
        userRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Cacheable(key = "'username:' + #p0")
    public UserDto findByName(String userName) {
        User user = userRepository.findByUsername(userName);
        if (user == null) {
            throw new EntityNotFoundException(User.class, "name", userName);
        } else {
            return userMapper.toDto(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePass(String username, String pass) {
        userRepository.updatePass(username, pass, new Date());
        redisUtils.del("user::username:" + username);
        flushCache(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> updateAvatar(MultipartFile multipartFile) {
        User user = userRepository.findByUsername(SecurityUtils.getCurrentUsername());
        String oldPath = user.getAvatarPath();
        File file = FileUtil.upload(multipartFile, properties.getPath().getAvatar());
        user.setAvatarPath(Objects.requireNonNull(file).getPath());
        user.setAvatarName(file.getName());
        userRepository.save(user);
        if (StringUtils.isNotBlank(oldPath)) {
            FileUtil.del(oldPath);
        }
        @NotBlank String username = user.getUsername();
        redisUtils.del(CacheKey.USER_NAME + username);
        flushCache(username);
        return new HashMap<String, String>(1) {{
            put("avatar", file.getName());
        }};
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String username, String email) {
        userRepository.updateEmail(username, email);
        redisUtils.del(CacheKey.USER_NAME + username);
        flushCache(username);
    }

    @Override
    public void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserDto userDTO : queryAll) {
            List<String> roles = userDTO.getRoles().stream().map(RoleSmallDto::getName).collect(Collectors.toList());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", userDTO.getUsername());
            map.put("角色", roles);
            map.put("部门", userDTO.getDept().getName());
            map.put("岗位", userDTO.getJobs().stream().map(JobSmallDto::getName).collect(Collectors.toList()));
            map.put("邮箱", userDTO.getEmail());
            map.put("状态", userDTO.getEnabled() ? "启用" : "禁用");
            map.put("手机号码", userDTO.getPhone());
            map.put("修改密码的时间", userDTO.getPwdResetTime());
            map.put("创建日期", userDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    /**
     * 清理缓存
     *
     * @param id /
     */
    public void delCaches(Long id, String username) {
        redisUtils.del(CacheKey.USER_ID + id);
        redisUtils.del(CacheKey.USER_NAME + username);
        flushCache(username);
    }

    /**
     * 清理 登陆时 用户缓存信息
     *
     * @param username /
     */
    private void flushCache(String username) {
        userCacheClean.cleanUserCache(username);
    }

}
