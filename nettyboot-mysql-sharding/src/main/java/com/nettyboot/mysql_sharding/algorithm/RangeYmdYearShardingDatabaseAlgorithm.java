/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nettyboot.mysql_sharding.algorithm;

import com.google.common.collect.Range;
import com.nettyboot.mysql_sharding.config.YmdConfig;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class RangeYmdYearShardingDatabaseAlgorithm implements RangeShardingAlgorithm<String> {

    private static final Logger logger = LoggerFactory.getLogger(RangeYmdYearShardingDatabaseAlgorithm.class);
    
    @Override
    public Collection<String> doSharding(final Collection<String> databaseNames, final RangeShardingValue<String> shardingValueRange) {
        Set<String> result = new LinkedHashSet<>();

        // 时间范围
        Range<String> range = shardingValueRange.getValueRange();
        // 获取年份闭区间， [2019, 2021]
        Range<Integer> yearRange = YmdConfig.getYmdYearRange(range);
        if(yearRange == null){
            throw new UnsupportedOperationException();
        }
        // 查找匹配数据库
        for (String each : databaseNames) {
            int year = Integer.parseInt(each.substring(each.length() - 4));
            if(yearRange.contains(year)){
                result.add(each);
            }
        }
        if(result.isEmpty()){
            throw new UnsupportedOperationException();
        }
        return result;
    }
}
