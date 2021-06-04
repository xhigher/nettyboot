package com.nettyboot.mysql_sharding;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.mysql.sharding.ShardingType;
import com.nettyboot.mysql.sharding.YamlDataSourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * [一句话描述该类的功能]
 *
 * @author : [Administrator]
 * @version : [v1.0]
 * @createTime : [2021/4/16 17:37]
 */
public class DbTest {

    private static final Logger logger = LoggerFactory.getLogger(DbTest.class);
    private static DataSource dataSource = null;
    private static Connection connection = null;

    @Before
    public void init() throws IOException, SQLException {
        dataSource = YamlDataSourceFactory.newInstance(ShardingType.SHARDING_DATABASES, "/demo/test-sharding-databases-range.yaml");

        connection = dataSource.getConnection();
    }

    @After
    public void destory() throws SQLException {
        if(connection != null){
            connection.close();
        }

    }


    @Test
    public void testSql(){
//        String sql = "SELECT * FROM t_order o RIGHT JOIN (SELECT MAX(`order_id`) AS `max_order_id` FROM t_order where ymd > '2019-12' and ymd <= '2021-02' ) AS tmp on o.`order_id`=tmp.max_order_id  where ymd > '2019-12' and ymd <= '2021-02'";
        String sql = "SELECT MAX(`id`) AS `max_id` FROM user_info where ymd > '2019-12' and ymd <= '2021-02'";

        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }

    @Test
    public void testSql2(){
//        String sql = "SELECT * FROM t_order o RIGHT JOIN (SELECT MAX(`order_id`) AS `max_order_id` FROM t_order where ymd > '2019-12' and ymd <= '2021-02' ) AS tmp on o.`order_id`=tmp.max_order_id  where ymd > '2019-12' and ymd <= '2021-02'";
        String sql = "SELECT MAX(u.`id`) AS `max_id` FROM user_info AS u Left join user_info_more as um on u.id=um.id where u.ymd > '2020-09' and u.ymd <= '2021-03'";

        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }

    @Test
    public void testSql3(){
//        String sql = "SELECT * FROM t_order o RIGHT JOIN (SELECT MAX(`order_id`) AS `max_order_id` FROM t_order where ymd > '2019-12' and ymd <= '2021-02' ) AS tmp on o.`order_id`=tmp.max_order_id  where ymd > '2019-12' and ymd <= '2021-02'";
        String sql = "SELECT * FROM user_info_current limit 10";
        for (int i = 0; i < 10; i++) {
            JSONArray objects = selectBySQL(sql);
            System.out.println(objects);
        }
    }

    @Test
    public void testSql4(){

        String sql = "SELECT `v`.`top_industryid`,`vb`.`industryid`,v.top_industry_name,vb.industry_name,v.video_tags,\n" +
                "      v.video_view_count,v.video_like_count,v.video_forward_count\n" +
                "      FROM `video_info_current` AS `v` INNER JOIN `video_industry2_rel` AS `vb` ON `v`.`video_aid` = `vb`.`videoid`\n" +
                "      WHERE v.`publish_ymd` > '2021-01-01'  AND v.`publish_ymd` <= '2021-05-01'  AND `vb`.`industryid` = 1";

        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }

    @Test
    public void testSql5(){

        String sql = "SELECT `v`.`top_industryid`,`vb`.`industryid`,v.top_industry_name,vb.industry_name,v.video_tags,\n" +
                "      v.video_view_count,v.video_like_count,v.video_forward_count\n" +
                "      FROM `video_info_current` AS `v` INNER JOIN `video_industry2_rel` AS `vb` ON `v`.`video_aid` = `vb`.`videoid`\n" +
                "      WHERE v.`publish_ymd` > '2021-01-01'  AND v.`publish_ymd` <= '2021-05-01'  AND `vb`.`industryid` = 1";

        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }

    @Test
    public void testSql6(){

        String sql = "SELECT `v`.`top_industryid`,`vb`.`industryid`,v.top_industry_name,vb.industry_name,v.video_tags,\n" +
                "      v.video_view_count,v.video_like_count,v.video_forward_count\n" +
                "      FROM `video_info` AS `v` INNER JOIN `video_industry2_rel` AS `vb` ON `v`.`video_aid` = `vb`.`videoid`\n" +
                "      WHERE v.`ymd` > '2020-12-01'  AND v.`ymd` <= '2021-05-01'  AND `vb`.`industryid` = 1";

        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }

    @Test
    public void testSql7(){

        String sql = "SELECT `v`.`top_industryid`,`vb`.`brandid`,v.top_industry_name,vb.brand_name,v.video_tags,\n" +
                "      v.video_view_count,v.video_like_count,v.video_forward_count\n" +
                "      FROM `video_info` AS `v` INNER JOIN `video_brand_rel` AS `vb` ON `v`.`video_aid` = `vb`.`videoid`\n" +
                "      WHERE v.`ymd` > '2020-12-01'  AND v.`ymd` <= '2021-05-01'  AND `vb`.`brandid` = 1";

        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }

    @Test
    public void testSql8(){

        String sql = "SELECT\n" +
                "\t`u`.`ymd`,\n" +
                "            `view_count`,\n" +
                "            `like_count`,\n" +
                "            `collect_count`,\n" +
                "            `um`.`comment_count`,\n" +
                "            `video_count`,\n" +
                "            `fans_count`,\n" +
                "            `view_count`,\n" +
                "            `danmaku_count`,\n" +
                "            `forward_count`,\n" +
                "            `coin_count`,\n" +
                "    row_number ( ) over ( PARTITION BY `u`.`ymd` ORDER BY `u`.`id` DESC ) AS `rn`\n" +
                "    FROM\n" +
                "\t`user_info` AS `u`\n" +
                "    LEFT JOIN `user_info_more` AS `um` ON `u`.`userid` = `um`.`userid`\n" +
                "    WHERE\n" +
                "`u`.`ymd` IN ( '2021-03-19', '2021-04-28' )\n" +
                "    AND `u`.userid = '10040906'";

        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }


    @Test
    public void testSql9(){

        String sql = "SELECT " +
                "`u`.`ymd`,`view_count`,`like_count`,`collect_count`,`um`.`comment_count`,`video_count`,`fans_count`," +
                "`view_count`,`danmaku_count`,`forward_count`,`coin_count`  " +
                "FROM `user_info` AS `u`  " +
                "INNER JOIN (  " +
                "SELECT MAX(`id`) AS `max_id`  " +
                "FROM `user_info` AS `u`  " +
                "WHERE `userid` = '352049471'  " +
                "AND `ymd` IN ('2021-02-17','2021-04-28') GROUP BY `ymd` " +
                ") AS `tmp` ON `u`.`id` = `tmp`.`max_id`  " +
                "LEFT JOIN `user_info_more_202104` AS `um` on `u`.`userid`=`um`.`userid`  " +
                "WHERE `u`.`ymd` IN ('2021-02-17','2021-04-28')";

        System.out.println(sql);
        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }


    @Test
    public void testSql10(){

        String sql = "SELECT " +
                "`u`.`ymd`,`view_count`,`like_count`,`collect_count`,`um`.`comment_count`,`video_count`,`fans_count`," +
                "`view_count`,`danmaku_count`,`forward_count`,`coin_count`,row_number ( ) over ( PARTITION BY `u`.`ymd` ORDER BY `u`.`id` DESC ) AS `rn`  " +
                "FROM `user_info` AS `u`  " +
//                "LEFT JOIN (  " +
//                "SELECT `id`  " +
//                "FROM `user_info` AS `u`  " +
//                "WHERE `userid` = '352049471'  " +
//                "AND `ymd` IN ('2021-02-17','2021-04-28') " +
//                ") AS `tmp` ON `u`.`id` = `tmp`.`max_id`  " +
                "LEFT JOIN `user_info_more` AS `um` on `u`.`userid`=`um`.`userid`  " +
                "WHERE `u`.`ymd` IN ('2021-02-17','2021-03-19')";

        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }

    @Test
    public void testSql11(){

        String sql = "SELECT " +
                "`u`.`ymd`,`view_count`,`like_count`,`collect_count`,`um`.`comment_count`,`video_count`,`fans_count`," +
                "`view_count`,`danmaku_count`,`forward_count`,`coin_count`  " +
                "FROM `user_info` AS `u`  " +
//                "INNER JOIN (  " +
//                "SELECT MAX(`id`) AS `max_id`  " +
//                "FROM `user_info` AS `u`  " +
//                "WHERE `userid` = '352049471'  " +
//                "AND `ymd` IN ('2021-02-17','2021-04-28') GROUP BY `ymd` " +
//                ") AS `tmp` ON `u`.`id` = `tmp`.`max_id`  " +
                "LEFT JOIN `user_info_more_202104` AS `um` on `u`.`userid`=`um`.`userid`  " +
                "WHERE `u`.`ymd` IN ('2021-02-17','2021-04-28')" +
                " AND `u`.`userid` = '352049471' ";

        JSONArray objects = selectBySQL(sql);
        System.out.println(objects);
    }

    public static JSONArray selectBySQL(String sql, Object... data) {
//        Connection conn = this.getConnection();
        if (connection == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(sql);
            if (data != null) {
                for (int i = 0; i < data.length; i++) {
                    pstmt.setObject(i + 1, data[i]);
                }
            }
            rs = pstmt.executeQuery();
            return getJSONArray(rs);
        } catch (SQLException e) {
            logger.error("XModel.selectBySQL.SQLException:" + sql, e);
        } finally {

//            this.closeConnection(rs, pstmt, conn);
        }
        return null;
    }


    protected static JSONArray getJSONArray(ResultSet rs) throws SQLException {
        return getJSONArray(rs, false);
    }

    protected static JSONArray getJSONArray(ResultSet rs, boolean single) throws SQLException {
        JSONArray data = new JSONArray();
        if (rs == null) {
            return data;
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int index = 0, type, count = metaData.getColumnCount();
        while (rs.next()) {
            if (single && count == 1) {
                type = metaData.getColumnType(1);
                if (Types.INTEGER == type || Types.TINYINT == type || Types.SMALLINT == type || Types.BIT == type) {
                    data.add(rs.getInt(1));
                } else if (Types.BIGINT == type) {
                    data.add(rs.getLong(1));
                } else if (Types.DOUBLE == type || Types.FLOAT == type || Types.DECIMAL == type) {
                    data.add(rs.getDouble(1));
                } else {
                    data.add(rs.getString(1));
                }
            } else {
                JSONObject item = new JSONObject();
                for (index = 1; index <= count; index++) {
                    type = metaData.getColumnType(index);
                    if (Types.INTEGER == type || Types.TINYINT == type || Types.SMALLINT == type || Types.BIT == type) {
                        item.put(metaData.getColumnLabel(index), rs.getInt(index));
                    } else if (Types.BIGINT == type) {
                        item.put(metaData.getColumnLabel(index), rs.getLong(index));
                    } else if (Types.DOUBLE == type || Types.FLOAT == type || Types.DECIMAL == type) {
                        item.put(metaData.getColumnLabel(index), rs.getDouble(index));
                    } else {
                        item.put(metaData.getColumnLabel(index), rs.getString(index));
                    }
                }
                data.add(item);
            }
        }
        rs.close();
        return data;
    }
}
