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
import me.zhengjie.modules.system.domain.SysSchool;
import me.zhengjie.modules.system.service.SysSchoolService;
import me.zhengjie.modules.system.service.dto.SysSchoolQueryCriteria;
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
* @date 2020-08-03
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "school管理")
@RequestMapping("/api/sysSchool")
public class SysSchoolController {

    private final SysSchoolService sysSchoolService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('sysSchool:list')")
    public void download(HttpServletResponse response, SysSchoolQueryCriteria criteria) throws IOException {
        sysSchoolService.download(sysSchoolService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询school")
    @ApiOperation("查询school")
    @PreAuthorize("@el.check('sysSchool:list')")
    public ResponseEntity<Object> query(SysSchoolQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(sysSchoolService.queryAll(criteria,pageable),HttpStatus.OK);
    }
    @Log("查询school")
    @ApiOperation("查询school")
    @GetMapping("/querySchool")
    public ResponseEntity<Object> query(SysSchoolQueryCriteria criteria){
        return new ResponseEntity<>(sysSchoolService.queryAll(criteria),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增school")
    @ApiOperation("新增school")
    @PreAuthorize("@el.check('sysSchool:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody SysSchool resources){
        return new ResponseEntity<>(sysSchoolService.create(resources),HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改school")
    @ApiOperation("修改school")
    @PreAuthorize("@el.check('sysSchool:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody SysSchool resources){
        sysSchoolService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除school")
    @ApiOperation("删除school")
    @PreAuthorize("@el.check('sysSchool:del')")
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Long[] ids) {
        sysSchoolService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}