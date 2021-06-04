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

package com.nettyboot.mysql.sharding.config.algorithm;

import com.google.common.collect.Range;
import com.nettyboot.mysql.sharding.config.util.YmdUtil;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class RangeYmdYearShardingTableAlgorithm implements RangeShardingAlgorithm<String> {

    /**
     * 年份分表，表明后缀长度
     * xxx2021 - xxx2023
     */
    private static final int TABLE_NAME_SUFFIX_LENGTH = 4;

    @Override
    public Collection<String> doSharding(final Collection<String> tableNames, final RangeShardingValue<String> shardingValue) {
        Set<String> result = new LinkedHashSet<>();

        // 查找时间范围
        Range<String> range = shardingValue.getValueRange();
        // 获取年月份闭区间， 如：[202001, 202111]
        Range<Integer> ymRange = YmdUtil.getYmdYearRange(range);
        if (ymRange == null) {
            throw new UnsupportedOperationException();
        }

        // 查找对应表
        for (String each : tableNames) {
            int ym = Integer.parseInt(each.substring(each.length() - TABLE_NAME_SUFFIX_LENGTH));
            if (ymRange.contains(ym)) {
                result.add(each);
            }
        }
        if (result.isEmpty()) {
//            throw new UnsupportedOperationException();
            return null;
        }
        return result;
    }
}


