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
import me.zhengjie.modules.system.service.ResultService;
import me.zhengjie.modules.system.service.dto.EndMatchReq;
import me.zhengjie.modules.system.service.dto.ResultQueryCriteria;
import me.zhengjie.modules.system.service.dto.StartMatchReq;
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
@Api(tags = "result管理")
@RequestMapping("/api/result")
public class ResultController {

    private final ResultService resultService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('result:list')")
    public void download(HttpServletResponse response, ResultQueryCriteria criteria) throws IOException {
        resultService.download(resultService.findAllOderByTotalAndCost(), response);
    }

    @GetMapping
    @Log("查询result")
    @ApiOperation("查询result")
    @PreAuthorize("@el.check('result:list')")
    public ResponseEntity<Object> query(ResultQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(resultService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @Log("新增result")
    @ApiOperation("新增result")
    @PostMapping(value = "/start")
    @PreAuthorize("@el.check('result:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody StartMatchReq startMatchReq){
        return new ResponseEntity<>(resultService.create(startMatchReq),HttpStatus.CREATED);
    }

    @PutMapping(value = "/end")
    @Log("修改result")
    @ApiOperation("修改result")
    @PreAuthorize("@el.check('result:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody EndMatchReq endMatchReq){
        return new ResponseEntity<>(resultService.update(endMatchReq),HttpStatus.OK);
    }

    @Log("删除result")
    @ApiOperation("删除result")
    @PreAuthorize("@el.check('result:del')")
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Long[] ids) {
        resultService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("查询个人成绩")
    @ApiOperation("查询个人成绩")
    @GetMapping(value = "/query")
    public ResponseEntity<Object> query(Long match_id,String phone) {
        return new ResponseEntity<>( resultService.findByUserIdAndMatchId(match_id,phone),HttpStatus.OK);
    }
}