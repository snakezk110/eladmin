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
package me.zhengjie.modules.system.repository;


import org.springframework.data.jpa.repository.Query;
import me.zhengjie.modules.system.domain.Result;
import me.zhengjie.modules.system.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
* @website https://el-admin.vip
* @author zach
* @date 2020-08-11
**/
public interface ResultRepository extends JpaRepository<Result, Long>, JpaSpecificationExecutor<Result> {

    /**
     * 根据userid 查询
     * @param userId 邮箱
     * @return /
     */
    Result findByUserIdAndMatchId(Long userId,Long matchId);


    @Query(value = "select * from result order by result_total DESC, cost_time ASC",nativeQuery = true)
    List<Result> findAllOderByTotalAndCost();
}