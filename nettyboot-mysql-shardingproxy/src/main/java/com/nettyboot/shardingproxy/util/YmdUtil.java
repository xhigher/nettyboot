package com.nettyboot.shardingproxy.util;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 操作ymd的类.
 *
 * @author : [lmr2015]
 * @version : [v1.0]
 * @
 */
public class YmdUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(YmdUtil.class);

    /**
     * 获取ymd范围的月份闭区间.
     */
    public static Range<Integer> getYmdMonthRange(final Range<String> range) {
        Integer closedStartYm = null;
        Integer closedEndYm = null;
        if (range.hasLowerBound()) {
            try {
                String startYmd = getClosedStartYmd(range.lowerBoundType(), range.lowerEndpoint());

                closedStartYm = Integer.parseInt(startYmd.replace("-", "").substring(0, 6));
            } catch (IllegalStateException e) {
                LOGGER.error("getYmdMonthRange.LowerBound.IllegalStateException", e);
            }
        }
        if (range.hasUpperBound()) {
            try {
                String endYmd = getClosedEndYmd(range.upperBoundType(), range.upperEndpoint());

                closedEndYm = Integer.parseInt(endYmd.replace("-", "").substring(0, 6));
            } catch (IllegalStateException e) {
                LOGGER.error("getYmdMonthRange.UpperBound.IllegalStateException", e);
            }
        }

        return getClosedRange(closedStartYm, closedEndYm);
    }

    /**
     * 获取ymd范围的年份闭区间.
     */
    public static Range<Integer> getYmdYearRange(Range<String> range) {
        Integer closedStartYear = null;
        Integer closedEndYear = null;
        if (range.hasLowerBound()) {
            try {
                String startYmd = getClosedStartYmd(range.lowerBoundType(), range.lowerEndpoint());

                closedStartYear = Integer.parseInt(startYmd.substring(0, 4));
            } catch (IllegalStateException e) {
                LOGGER.error("getYmdYearRange.LowerBound.IllegalStateException", e);
            }
        }
        if (range.hasUpperBound()) {
            try {
                String endYmd = getClosedEndYmd(range.upperBoundType(), range.upperEndpoint());

                closedEndYear = Integer.parseInt(endYmd.substring(0, 4));
            } catch (IllegalStateException e) {
                LOGGER.error("getYmdYearRange.UpperBound.IllegalStateException", e);
            }
        }

        return getClosedRange(closedStartYear, closedEndYear);
    }

    /**
     * 获取开始边界值.
     * @param boundType
     * @param ymd
     * @return
     */
    public static String getClosedStartYmd(BoundType boundType, String ymd) {
        try {
            String[] ymds = ymd.split("-");
            int year = Integer.parseInt(ymds[0]);
            int month;
            if (ymds.length > 1) {
                month = Integer.parseInt(ymds[1]);
            } else {
                month = 1;
            }
            int day;
            if (ymds.length > 2) {
                day = Integer.parseInt(ymds[2]);
            } else {
                // 当起始值没有天数值时，加上一天，边界类型改为CLOSED，如 (2021-01, 2021-02] 等同于 [2021-01-01, 2021-02]
                day = 1;
                boundType = BoundType.CLOSED;
            }

            String newYmd;
            if (boundType == BoundType.OPEN) {
                newYmd = getAfterDaysYmd(1, getYmd(year, month, day));
            } else {
                newYmd = getYmd(year, month, day);
            }
            return newYmd;
        } catch (NumberFormatException ignore) {

        }
        return ymd;
    }

    /**
     * 获取结束边界值.
     * @param boundType
     * @param ymd
     * @return
     */
    public static String getClosedEndYmd(BoundType boundType, String ymd) {
        try {
            String[] ymds = ymd.split("-");
            int year = Integer.parseInt(ymds[0]);
            int month;
            if (ymds.length > 1) {
                month = Integer.parseInt(ymds[1]);
            } else {
                month = 1;
            }
            int day;
            if (ymds.length > 2) {
                day = Integer.parseInt(ymds[2]);
            } else {
                // 当结束值没有天数值时，加上一天，边界类型改为OPEN，如 (2021-01, 2021-02] 等同于 (2021-01, 2021-02-01)
                day = 1;
                boundType = BoundType.OPEN;
            }

            String newYmd;
            if (boundType == BoundType.OPEN) {
                newYmd = getAfterDaysYmd(-1, getYmd(year, month, day));
            } else {
                newYmd = getYmd(year, month, day);
            }
            return newYmd;
        } catch (NumberFormatException ignore) {

        }
        return ymd;
    }

    /**
     * 获取闭区间的值.
     * @param startNum
     * @param endNum
     * @return
     */
    public static Range<Integer> getClosedRange(Integer startNum, Integer endNum) {
        if (startNum == null && endNum == null) {
            // 没有开始和结束值时，返回全量的值
            return Range.all();
        }
        Range<Integer> range;
        if (startNum != null && endNum == null) {
            // 只有开始边界
            range = Range.atLeast(startNum);
        } else if (startNum == null) {
            // 只有结束边界
            range = Range.atMost(endNum);
        } else {
            // 开始和结束都存在值
            range = Range.closed(startNum, endNum);
        }
        return range;
    }

    private static final int APPEND_0_NUMBER = 10;

    /**
     * 拼接ymd.
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static String getYmd(int year, int month, int day) {
        StringBuilder ymd = new StringBuilder();
        ymd.append(year);
        ymd.append("-");
        if (month < APPEND_0_NUMBER) {
            ymd.append(0);
        }
        ymd.append(month);
        ymd.append("-");
        if (day < APPEND_0_NUMBER) {
            ymd.append(0);
        }
        ymd.append(day);
        return ymd.toString();
    }


    private static final DateTimeFormatter Y_M_D = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    /**
     * 获取n天后的日期
     */
    public static String getAfterDaysYmd(int days, String startDate) {
        String endDate = null;
        try {
            DateTimeFormatter formatter = startDate.length() == 8 ? YMD : Y_M_D;
            LocalDate date1 = LocalDate.parse(startDate, formatter);
            LocalDate date2 = date1.plusDays(days);
            endDate = date2.format(formatter);
        } catch (Exception e) {
        }
        return endDate;
    }
}
