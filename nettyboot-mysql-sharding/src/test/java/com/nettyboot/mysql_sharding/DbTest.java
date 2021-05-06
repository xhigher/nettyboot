package com.nettyboot.mysql_sharding;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.mysql_sharding.config.ShardingType;
import com.nettyboot.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Properties;

/**
 * [一句话描述该类的功能]
 *
 * @author : [Administrator]
 * @version : [v1.0]
 * @createTime : [2021/4/16 17:37]
 */
public class DbTest {

    private static final String PROPERTIES_FILEPATH = "/application.properties";
    private static DataSource dataSource = null;
    private static Connection connection = null;

    @Before
    public void init() throws IOException, SQLException {
        Properties properties = FileUtil.getProperties(PROPERTIES_FILEPATH);
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
    public void test(){
        String sql = "SELECT COUNT(1) FROM t_order where ymd >= '2019-02' and ymd < '2021-02'";

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
//            logger.error("XModel.selectBySQL.SQLException:" + sql, e);
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
