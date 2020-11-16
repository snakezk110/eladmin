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
package me.zhengjie.modules.system.service.dto;

import cn.hutool.json.JSONObject;
import lombok.Data;
import me.zhengjie.modules.system.domain.User;

import java.io.Serializable;
import java.sql.Timestamp;

/**
* @website https://el-admin.vip
* @description /
* @author zach
* @date 2020-08-11
**/
@Data
public class ResultDto implements Serializable {

    /** 成绩ID */
    private Long resultId;

    /** 成绩总分 */
    private Double resultTotal;

    /** 详细成绩 */
    private String result;

    /** 用户 */
    private User user;

    private Timestamp startTime;

    private Timestamp endTime;

    private Long costTime;

    private String result_1;
    private String result_2;
    private String result_3;
    private String result_4;
    private String result_5;
    private String result_6;
    private String result_7;
    private String result_8;
    private String result_9;
    private String result_10;

    public String getResult_1() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_1"));
    }

    public String getResult_2() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_2"));
    }

    public String getResult_3() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_3"));
    }

    public String getResult_4() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_4"));
    }

    public String getResult_5() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_5"));
    }

    public String getResult_6() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_6"));
    }

    public String getResult_7() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_7"));
    }

    public String getResult_8() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_8"));
    }

    public String getResult_9() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_9"));
    }

    public String getResult_10() {
        JSONObject jsonObj=new JSONObject(result);
        return String.valueOf(jsonObj.get("result_10"));
    }





}