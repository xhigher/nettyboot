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

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class RangeYmdMonthShardingTableAlgorithm implements RangeShardingAlgorithm<String> {

    private static final Logger logger = LoggerFactory.getLogger(RangeYmdMonthShardingTableAlgorithm.class);

    @Override
    public Collection<String> doSharding(final Collection<String> tableNames, final RangeShardingValue<String> shardingValue) {
        Set<String> result = new LinkedHashSet<>();

        // 查找时间范围
        Range<String> range = shardingValue.getValueRange();
        Integer startYm = null;
        Integer endYm = null;
        if(range.hasLowerBound()){
            try {
                startYm = Integer.parseInt(range.lowerEndpoint().replace("-", "").substring(0, 6));
            }catch (IllegalStateException e){
                logger.error("RangeYmdYearShardingDatabaseAlgorithm.doSharding.IllegalStateException", e);
            }catch (Exception e){
                logger.error("RangeYmdYearShardingDatabaseAlgorithm.doSharding.Exception", e);
            }
        }
        if(range.hasUpperBound()){
            try {
                endYm = Integer.parseInt(range.upperEndpoint().replace("-", "").substring(0, 6));
            }catch (IllegalStateException e){
                logger.error("RangeYmdYearShardingDatabaseAlgorithm.doSharding.IllegalStateException", e);
            }catch (Exception e){
                logger.error("RangeYmdYearShardingDatabaseAlgorithm.doSharding.Exception", e);
            }
        }

        // 查找对应表
        for (String each : tableNames) {
            int ym = Integer.parseInt(each.substring(each.length() - 6));
            boolean startFlag = false;
            boolean endFlag = false;
            if(startYm != null){
                if(ym > startYm || (range.lowerBoundType() == BoundType.CLOSED && ym == startYm)){
                    startFlag = true;
                }
            }else{
                startFlag = true;
            }
            if(endYm != null){
                if(ym < endYm || (range.upperBoundType() == BoundType.CLOSED && ym == endYm)){
                    endFlag = true;
                }
            }else {
                endFlag = true;
            }
            if (startFlag && endFlag) {
                result.add(each);
            }
        }
        if(result.isEmpty()){
            throw new UnsupportedOperationException();
        }
        return result;
    }
}


