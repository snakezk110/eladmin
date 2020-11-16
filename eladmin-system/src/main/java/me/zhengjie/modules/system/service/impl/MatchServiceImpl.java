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

import me.zhengjie.modules.system.domain.Match;
import me.zhengjie.modules.system.repository.MatchRepository;
import me.zhengjie.modules.system.service.MatchService;
import me.zhengjie.modules.system.service.dto.MatchDto;
import me.zhengjie.modules.system.service.dto.MatchQueryCriteria;
import me.zhengjie.modules.system.service.mapstruct.MatchMapper;
import me.zhengjie.utils.ValidationUtil;
import me.zhengjie.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
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
* @date 2020-08-11
**/
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;

    @Override
    public Map<String,Object> queryAll(MatchQueryCriteria criteria, Pageable pageable){
        Page<Match> page = matchRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(matchMapper::toDto));
    }

    @Override
    public List<MatchDto> queryAll(MatchQueryCriteria criteria){
        return matchMapper.toDto(matchRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    @Transactional
    public MatchDto findById(Long matchId) {
        Match match = matchRepository.findById(matchId).orElseGet(Match::new);
        ValidationUtil.isNull(match.getMatchId(),"Match","matchId",matchId);
        return matchMapper.toDto(match);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MatchDto create(Match resources) {
        Snowflake snowflake = IdUtil.createSnowflake(1, 1);
        resources.setMatchId(snowflake.nextId()); 
        return matchMapper.toDto(matchRepository.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Match resources) {
        Match match = matchRepository.findById(resources.getMatchId()).orElseGet(Match::new);
        ValidationUtil.isNull( match.getMatchId(),"Match","id",resources.getMatchId());
        match.copy(resources);
        matchRepository.save(match);
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long matchId : ids) {
            matchRepository.deleteById(matchId);
        }
    }

    @Override
    public void download(List<MatchDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (MatchDto match : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("比赛名称", match.getMatchName());
            map.put("开始时间", match.getStartTime());
            map.put("结束时间", match.getEndTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}