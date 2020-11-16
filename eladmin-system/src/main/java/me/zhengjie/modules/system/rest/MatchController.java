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
package me.zhengjie.modules.system.rest;

import me.zhengjie.annotation.Log;
import me.zhengjie.modules.system.domain.Match;
import me.zhengjie.modules.system.service.MatchService;
import me.zhengjie.modules.system.service.dto.MatchQueryCriteria;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
* @website https://el-admin.vip
* @author zach
* @date 2020-08-11
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "match管理")
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('match:list')")
    public void download(HttpServletResponse response, MatchQueryCriteria criteria) throws IOException {
        matchService.download(matchService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询match")
    @ApiOperation("查询match")
    @PreAuthorize("@el.check('match:list')")
    public ResponseEntity<Object> query(MatchQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(matchService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增match")
    @ApiOperation("新增match")
    @PreAuthorize("@el.check('match:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Match resources){
        return new ResponseEntity<>(matchService.create(resources),HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改match")
    @ApiOperation("修改match")
    @PreAuthorize("@el.check('match:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody Match resources){
        matchService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除match")
    @ApiOperation("删除match")
    @PreAuthorize("@el.check('match:del')")
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Long[] ids) {
        matchService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}