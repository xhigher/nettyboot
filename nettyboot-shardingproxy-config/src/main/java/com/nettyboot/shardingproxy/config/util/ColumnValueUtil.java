package com.nettyboot.shardingproxy.config.util;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

/**
 * [一句话描述该类的功能]
 *
 * @author : [Administrator]
 * @version : [v1.0]
 * @createTime : [2021/5/24 17:29]
 */
public class ColumnValueUtil {

    public static long getLongColumnLongValue(final PreciseShardingValue<Long> shardingValue){
        long value = 0L;
        try {
            Object valueObj = shardingValue.getValue();
            Class valueClazz = valueObj.getClass();
            if(valueClazz == Integer.class){
                value = Long.parseLong(valueObj.toString());
            }else if(valueClazz == String.class){
                value = Long.parseLong((String) valueObj);
            }else{
                value = shardingValue.getValue();
            }
        } catch (NumberFormatException e) {

        }
        return value;
    }

    public static long getIntegerColumnLongValue(final PreciseShardingValue<Integer> shardingValue){
        long value = 0L;
        try {
            Object valueObj = shardingValue.getValue();
            Class valueClazz = valueObj.getClass();
            if(valueClazz == Integer.class){
                value = Long.parseLong(valueObj.toString());
            }else if(valueClazz == String.class){
                value = Long.parseLong((String) valueObj);
            }else{
                value = shardingValue.getValue();
            }
        } catch (NumberFormatException e) {

        }
        return value;
    }

}
