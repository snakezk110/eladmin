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

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.sql.Timestamp;

/**
* @website https://el-admin.vip
* @description /
* @author zach
* @date 2020-08-11
**/
@Entity
@Data
@Table(name="result")
public class Result implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    @ApiModelProperty(value = "成绩ID")
    private Long resultId;

    @Column(name = "result_total",nullable = false)
    @NotNull
    @ApiModelProperty(value = "成绩总分")
    private Double resultTotal;

    @Column(name = "result")
    @ApiModelProperty(value = "详细成绩")
    private String result;

    @OneToOne
    @JoinColumn(name = "user_id")
    @ApiModelProperty(value = "用户")
    @JsonIgnore
    private User user;

    @Column(name = "match_id",nullable = false)
    @NotNull
    @ApiModelProperty(value = "比赛ID")
    private Long matchId;

    @Column(name = "start_time")
    @ApiModelProperty(value = "开始考试时间")
    private Timestamp startTime;

    @Column(name = "end_time")
    @ApiModelProperty(value = "结束考试时间")
    private Timestamp endTime;

    @Column(name = "cost_time")
    @ApiModelProperty(value = "消耗时间")
    private Long costTime;

    public void copy(Result source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}