package com.nettyboot.mysql_sharding.config;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.nettyboot.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 操作ymd的类
 *
 * @author : [Administrator]
 * @version : [v1.0]
 * @createTime : [2021/5/6 17:45]
 */
public class YmdConfig {

    private static final Logger logger = LoggerFactory.getLogger(YmdConfig.class);

    // 获取ymd范围的月份闭区间
    public static Range<Integer> getYmdMonthRange(Range<String> range){
        Integer closedStartYm = null;
        Integer closedEndYm = null;
        if(range.hasLowerBound()){
            try {
                String startYmd = YmdConfig.getClosedStartYmd(range.lowerBoundType(), range.lowerEndpoint());

                closedStartYm = Integer.parseInt(startYmd.replace("-", "").substring(0, 6));
            }catch (IllegalStateException e){
                logger.error("getYmdMonthRange.LowerBound.IllegalStateException", e);
            }catch (Exception e){
                logger.error("getYmdMonthRange.LowerBound.Exception", e);
            }
        }
        if(range.hasUpperBound()){
            try {
                String endYmd = YmdConfig.getClosedEndYmd(range.upperBoundType(), range.upperEndpoint());

                closedEndYm = Integer.parseInt(endYmd.replace("-", "").substring(0, 6));
            }catch (IllegalStateException e){
                logger.error("getYmdMonthRange.UpperBound.IllegalStateException", e);
            }catch (Exception e){
                logger.error("getYmdMonthRange.UpperBound.Exception", e);
            }
        }

        return YmdConfig.getClosedRange(closedStartYm, closedEndYm);
    }

    // 获取ymd范围的年份闭区间
    public static Range<Integer> getYmdYearRange(Range<String> range){
        Integer closedStartYear = null;
        Integer closedEndYear = null;
        if(range.hasLowerBound()){
            try {
                String startYmd = YmdConfig.getClosedStartYmd(range.lowerBoundType(), range.lowerEndpoint());

                closedStartYear = Integer.parseInt(startYmd.substring(0, 4));
            }catch (IllegalStateException e){
                logger.error("getYmdYearRange.LowerBound.IllegalStateException", e);
            }catch (Exception e){
                logger.error("getYmdYearRange.LowerBound.Exception", e);
            }
        }
        if(range.hasUpperBound()){
            try {
                String endYmd = YmdConfig.getClosedEndYmd(range.upperBoundType(), range.upperEndpoint());

                closedEndYear = Integer.parseInt(endYmd.substring(0, 4));
            }catch (IllegalStateException e){
                logger.error("getYmdYearRange.UpperBound.IllegalStateException", e);
            }catch (Exception e){
                logger.error("getYmdYearRange.UpperBound.Exception", e);
            }
        }

        return YmdConfig.getClosedRange(closedStartYear, closedEndYear);
    }

    // 获取开始边界值
    public static String getClosedStartYmd(BoundType boundType, String ymd){
        try {
            String[] ymds = ymd.split("-");
            int year = Integer.parseInt(ymds[0]);
            int month = 0;
            if(ymds.length > 1){
                month = Integer.parseInt(ymds[1]);
            }else{
                month = 1;
            }
            int day = 0;
            if(ymds.length > 2){
                day = Integer.parseInt(ymds[2]);
            }else{
                // 当起始值没有天数值时，加上一天，边界类型改为CLOSED，如 (2021-01, 2021-02] 等同于 [2021-01-01, 2021-02]
                day = 1;
                boundType = BoundType.CLOSED;
            }

            String newYmd = null;
            if(boundType == BoundType.OPEN){
                newYmd = TimeUtil.getAfterDaysYMD(1, getYmd(year, month, day));
            }else {
                newYmd = getYmd(year, month, day);
            }
            return newYmd;
        } catch (NumberFormatException e) {

        }
        return ymd;
    }

    // 获取结束边界值
    public static String getClosedEndYmd(BoundType boundType, String ymd){
        try {
            String[] ymds = ymd.split("-");
            int year = Integer.parseInt(ymds[0]);
            int month = 0;
            if(ymds.length > 1){
                month = Integer.parseInt(ymds[1]);
            }else{
                month = 1;
            }
            int day = 0;
            if(ymds.length > 2){
                day = Integer.parseInt(ymds[2]);
            }else{
                // 当结束值没有天数值时，加上一天，边界类型改为OPEN，如 (2021-01, 2021-02] 等同于 (2021-01, 2021-02-01)
                day = 1;
                boundType = BoundType.OPEN;
            }

            String newYmd = null;
            if(boundType == BoundType.OPEN){
                newYmd = TimeUtil.getAfterDaysYMD(-1, getYmd(year, month, day));
            }else {
                newYmd = getYmd(year, month, day);
            }
            return newYmd;
        } catch (NumberFormatException e) {

        }
        return ymd;
    }

    // 获取闭区间的值
    public static Range<Integer> getClosedRange(Integer startNum, Integer endNum){
        if(startNum == null && endNum == null){
            // 没有开始和结束值时，返回全量的值
            return Range.all();
        }
        Range<Integer> range = null;
        if(startNum != null && endNum == null){
            // 只有开始边界
            range = Range.atLeast(startNum);
        }else if(startNum == null && endNum != null){
            // 只有结束边界
            range = Range.atMost(endNum);
        }else{
            // 开始和结束都存在值
            range = Range.closed(startNum, endNum);
        }
        return range;
    }

    // 拼接ymd
    public static String getYmd(int year, int month, int day){
        StringBuilder ymd = new StringBuilder();
        ymd.append(year);
        ymd.append("-");
        if(month < 10){
            ymd.append(0);
        }
        ymd.append(month);
        ymd.append("-");
        if(day < 10){
            ymd.append(0);
        }
        ymd.append(day);
        return ymd.toString();
    }

}
