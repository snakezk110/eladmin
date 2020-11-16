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
package me.zhengjie.modules.system.domain;

import lombok.Data;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.annotations.ApiModelProperty;
import cn.hutool.core.bean.copier.CopyOptions;
import javax.persistence.*;
import javax.validation.constraints.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.*;
import java.io.Serializable;

/**
* @website https://el-admin.vip
* @description /
* @author zach
* @date 2020-08-03
**/
@Entity
@Data
@Table(name="sys_school")
public class SysSchool implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "school_id")
    @ApiModelProperty(value = "schoolId")
    private Long schoolId;

    @Column(name = "school_name",nullable = false)
    @NotBlank
    @ApiModelProperty(value = "学校名称")
    private String schoolName;

    @Column(name = "school_description")
    @ApiModelProperty(value = "学校描述")
    private String schoolDescription;

    public void copy(SysSchool source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}