package com.nettyboot.mysql;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class XMySQL {

	private static final Logger logger = LoggerFactory.getLogger(XMySQL.class);

	private static final Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
	public static final List<String> poolNameList = new ArrayList<>();

	private static boolean initStarted = false;
	private static boolean initOK = false;

	public static void init(Properties properties) {
		if(properties.containsKey("mysql.status") && 1==Integer.valueOf(properties.getProperty("mysql.status").trim())) {
			if(initStarted || initOK){
				return;
			}
			initStarted = true;
			int size = Integer.valueOf(properties.getProperty("mysql.dataSource.size").trim());
			for (int i = 1; i <= size; i++) {
				HikariDataSource dataSource = createDataSource(properties, i);

				String poolName = dataSource.getPoolName();
				dataSourceMap.put(poolName, dataSource);

				if(!poolNameList.contains(poolName)){
					poolNameList.add(poolName);
				}
			}
			initOK = true;
		}
	}

	private static HikariDataSource createDataSource(Properties properties, int dbIndex){
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(properties.getProperty("mysql.dataSource" + dbIndex + ".driverClassName"));
		dataSource.setPoolName(properties.getProperty("mysql.dataSource" + dbIndex + ".name"));
		dataSource.setJdbcUrl(properties.getProperty("mysql.dataSource" + dbIndex + ".url"));
		dataSource.setUsername(properties.getProperty("mysql.dataSource" + dbIndex + ".user"));
		dataSource.setPassword(properties.getProperty("mysql.dataSource" + dbIndex + ".password"));
		dataSource.addDataSourceProperty("cachePrepStmts", properties.getProperty("mysql.dataSource" + dbIndex + ".cachePrepStmts"));
		dataSource.addDataSourceProperty("prepStmtCacheSize", properties.getProperty("mysql.dataSource" + dbIndex + ".prepStmtCacheSize"));
		dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", properties.getProperty("mysql.dataSource" + dbIndex + ".prepStmtCacheSqlLimit"));
		dataSource.setMaximumPoolSize(Integer.valueOf(properties.getProperty("mysql.dataSource" + dbIndex + ".maximumPoolSize")));
		dataSource.setConnectionInitSql(properties.getProperty("mysql.dataSource" + dbIndex + ".connectionInitSql", "SET NAMES utf8"));
		checkDataSource(dataSource);
		return dataSource;
	}
	
	private static void checkDataSource(HikariDataSource dataSource){
		try {
			Connection conn = dataSource.getConnection();
			if(conn != null){
				conn.close();
			}
		} catch (SQLException e) {
			logger.error("XMySQL.checkDataSource.SQLException:", e);
		}
	}

	public static DataSource getDataSource(String dsName) {
		try {
			return dataSourceMap.get(dsName);
		} catch (Exception e) {
			logger.error("XMySQL.getDataSource.Exception:", e);
		}
		return null;
	}

	public static Connection getConnection(String dsName) {
		try {
			return dataSourceMap.get(dsName).getConnection();
		} catch (SQLException e) {
			logger.error("XMySQL.getConnection.SQLException:", e);
		}
		return null;
	}

	public static void releaseConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("XMySQL.releaseConnection.SQLException:", e);
			}
		}
	}

}
