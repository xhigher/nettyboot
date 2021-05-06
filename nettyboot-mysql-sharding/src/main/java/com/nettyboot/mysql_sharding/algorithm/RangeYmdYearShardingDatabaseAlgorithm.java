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

public final class RangeYmdYearShardingDatabaseAlgorithm implements RangeShardingAlgorithm<String> {

    private static final Logger logger = LoggerFactory.getLogger(RangeYmdYearShardingDatabaseAlgorithm.class);
    
    @Override
    public Collection<String> doSharding(final Collection<String> databaseNames, final RangeShardingValue<String> shardingValueRange) {
        Set<String> result = new LinkedHashSet<>();

        Range<String> range = shardingValueRange.getValueRange();
        Integer startYear = null;
        Integer endYear = null;
        if(range.hasLowerBound()){
            try {
                startYear = Integer.parseInt(range.lowerEndpoint().substring(0, 4));
            }catch (IllegalStateException e){
                logger.error("RangeYmdYearShardingDatabaseAlgorithm.doSharding.IllegalStateException", e);
            }catch (Exception e){
                logger.error("RangeYmdYearShardingDatabaseAlgorithm.doSharding.Exception", e);
            }
        }
        if(range.hasUpperBound()){
            try {
                endYear = Integer.parseInt(range.upperEndpoint().substring(0, 4));
            }catch (IllegalStateException e){
                logger.error("RangeYmdYearShardingDatabaseAlgorithm.doSharding.IllegalStateException", e);
            }catch (Exception e){
                logger.error("RangeYmdYearShardingDatabaseAlgorithm.doSharding.Exception", e);
            }
        }

        // 查找拼配数据库
        for (String each : databaseNames) {
            int year = Integer.parseInt(each.substring(each.length() - 4));
            boolean startFlag = false;
            boolean endFlag = false;
            if(startYear != null){
                if(year > startYear || (range.lowerBoundType() == BoundType.CLOSED && year == startYear)){
                    startFlag = true;
                }
            }else{
                startFlag = true;
            }
            if(endYear != null){
                if(year < endYear || (range.upperBoundType() == BoundType.CLOSED && year == endYear)){
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
