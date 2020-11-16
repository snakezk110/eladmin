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

import me.zhengjie.modules.system.domain.SysSchool;
import me.zhengjie.modules.system.repository.SysSchoolRepository;
import me.zhengjie.modules.system.service.SysSchoolService;
import me.zhengjie.modules.system.service.dto.SysSchoolDto;
import me.zhengjie.modules.system.service.dto.SysSchoolQueryCriteria;
import me.zhengjie.modules.system.service.mapstruct.SysSchoolMapper;
import me.zhengjie.utils.ValidationUtil;
import me.zhengjie.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
* @website https://el-admin.vip
* @description 服务实现
* @author zach
* @date 2020-08-03
**/
@Service
@RequiredArgsConstructor
public class SysSchoolServiceImpl implements SysSchoolService {

    private final SysSchoolRepository sysSchoolRepository;
    private final SysSchoolMapper sysSchoolMapper;

    @Override
    public Map<String,Object> queryAll(SysSchoolQueryCriteria criteria, Pageable pageable){
        Page<SysSchool> page = sysSchoolRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(sysSchoolMapper::toDto));
    }

    @Override
    public List<SysSchoolDto> queryAll(SysSchoolQueryCriteria criteria){
        return sysSchoolMapper.toDto(sysSchoolRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    @Transactional
    public SysSchoolDto findById(Long schoolId) {
        SysSchool sysSchool = sysSchoolRepository.findById(schoolId).orElseGet(SysSchool::new);
        ValidationUtil.isNull(sysSchool.getSchoolId(),"SysSchool","schoolId",schoolId);
        return sysSchoolMapper.toDto(sysSchool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysSchoolDto create(SysSchool resources) {
        return sysSchoolMapper.toDto(sysSchoolRepository.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysSchool resources) {
        SysSchool sysSchool = sysSchoolRepository.findById(resources.getSchoolId()).orElseGet(SysSchool::new);
        ValidationUtil.isNull( sysSchool.getSchoolId(),"SysSchool","id",resources.getSchoolId());
        sysSchool.copy(resources);
        sysSchoolRepository.save(sysSchool);
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long schoolId : ids) {
            sysSchoolRepository.deleteById(schoolId);
        }
    }

    @Override
    public void download(List<SysSchoolDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysSchoolDto sysSchool : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put(" 学校名称",  sysSchool.getSchoolName());
            map.put(" 学校描述",  sysSchool.getSchoolDescription());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}