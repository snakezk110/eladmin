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

import cn.hutool.json.JSONObject;
import me.zhengjie.modules.system.domain.Result;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.repository.ResultRepository;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.system.service.MatchService;
import me.zhengjie.modules.system.service.ResultService;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.dto.*;
import me.zhengjie.modules.system.service.mapstruct.ResultMapper;
import me.zhengjie.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.*;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
* @website https://el-admin.vip
* @description 服务实现
* @author zach
* @date 2020-08-11
**/
@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;
    private final MatchService matchService;
    private final UserService userService;
    private final UserRepository userRepository;
    @Override
    public Map<String,Object> queryAll(ResultQueryCriteria criteria, Pageable pageable){
        Page<Result> page = resultRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(resultMapper::toDto));
    }

    @Override
    public List<ResultDto> queryAll(ResultQueryCriteria criteria){
        return resultMapper.toDto(resultRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    @Transactional
    public ResultDto findById(Long resultId) {
        Result result = resultRepository.findById(resultId).orElseGet(Result::new);
        ValidationUtil.isNull(result.getResultId(),"Result","resultId",resultId);
        return resultMapper.toDto(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object create(StartMatchReq startMatchReq) {
        Map<String, Object> respMap = new HashMap<>();
        respMap.put("status", -1);
        MatchDto matchDto =matchService.findById(startMatchReq.getMatch_id());
        User user=userRepository.findByPhone(startMatchReq.getPhone());
        Result result=resultRepository.findByUserIdAndMatchId(user.getId(),startMatchReq.getMatch_id());
        if(null!=result && result.getResultId()>0){
            respMap.put("status", HttpStatus.OK.value());
            respMap.put("message","比赛已经开始！");
            return respMap;
        }

        Timestamp current = new Timestamp(System.currentTimeMillis());
        if(current.getTime()<=matchDto.getStartTime().getTime()){
            respMap.put("message","比赛时间尚未开始！");
            return respMap;
        }
        Result resources=new Result();
        resources.setResultTotal(0.0);
        resources.setStartTime(current);
        resources.setMatchId(matchDto.getMatchId());
        resources.setUser(user);
        respMap.put("status", HttpStatus.OK.value());
        respMap.put("message", "比赛开始！");
        resultRepository.save(resources);
        return respMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object update(EndMatchReq endMatchReq) {
        User user=userRepository.findByPhone(endMatchReq.getPhone());
        Result result1=resultRepository.findByUserIdAndMatchId(user.getId(),endMatchReq.getMatch_id());
        Result result = resultRepository.findById(result1.getResultId()).orElseGet(Result::new);
        ValidationUtil.isNull(result.getResultId(),"Result","id",result1.getResultId());
        result.setResultTotal(endMatchReq.getResult_total());
        JSONObject jsonObj=new JSONObject(endMatchReq.getResult());
        result.setResult(jsonObj.toString());
        Timestamp current = new Timestamp(System.currentTimeMillis());
        result.setEndTime(current);
        Long costTime=result.getEndTime().getTime()-result.getStartTime().getTime();
        result.setCostTime(costTime/1000/60);
        resultRepository.save(result);
        Map<String, Object> respMap = new HashMap<>();
        respMap.put("status", HttpStatus.OK.value());
        respMap.put("message", "成绩上传成功！");
        return respMap;
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long resultId : ids) {
            resultRepository.deleteById(resultId);
        }
    }

    @Override
    public void download(List<ResultDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ResultDto result : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("用户", result.getUser().getNickName());
            map.put("成绩总分", result.getResultTotal());
            map.put("耗时",result.getCostTime()+"分钟");
            map.put("学校名称",result.getUser().getSchool().getSchoolName());
            JSONObject jsonObj=new JSONObject(result.getResult());
            for(int i=1;i<11;i++){
                map.put("题目_"+i, jsonObj.get("result_"+i));
            }
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Result findByUserIdAndMatchId(Long match_id, String phone) {
        User user=userRepository.findByPhone(phone);
        return resultRepository.findByUserIdAndMatchId(user.getId(),match_id);
    }

    @Override
    public List<ResultDto> findAllOderByTotalAndCost() {
        return resultMapper.toDto(resultRepository.findAllOderByTotalAndCost());
    }
}