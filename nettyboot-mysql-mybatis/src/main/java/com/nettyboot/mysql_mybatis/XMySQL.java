package com.nettyboot.mysql_mybatis;

import com.nettyboot.util.FileUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.mybatis.mapper.mapperhelper.MapperHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class XMySQL {

	private static final Logger logger = LoggerFactory.getLogger(XMySQL.class);

	private static final Map<String, SqlSessionFactory> sqlSessionMap = new HashMap<String, SqlSessionFactory>();

	private static boolean initStarted = false;
	private static boolean initOK = false;

	public static void init(Properties properties){
		if(properties.containsKey("mysql.status") && 1==Integer.valueOf(properties.getProperty("mysql.status").trim())) {
			if(initStarted || initOK){
				return;
			}
			initStarted = true;
			int size = Integer.valueOf(properties.getProperty("mysql.dataSource.size").trim());
			for (int i = 1; i <= size; i++) {
				String poolName = properties.getProperty("mysql.dataSource" + i + ".name");
				HikariDataSource dataSource = com.nettyboot.mysql.XMySQL.getDataSource(poolName);
				if(dataSource == null){
					dataSource = new HikariDataSource();
					dataSource.setDriverClassName(properties.getProperty("mysql.dataSource" + i + ".driverClassName"));
					dataSource.setPoolName(properties.getProperty("mysql.dataSource" + i + ".name"));
					dataSource.setJdbcUrl(properties.getProperty("mysql.dataSource" + i + ".url"));
					dataSource.setUsername(properties.getProperty("mysql.dataSource" + i + ".user"));
					dataSource.setPassword(properties.getProperty("mysql.dataSource" + i + ".password"));
					dataSource.addDataSourceProperty("cachePrepStmts", properties.getProperty("mysql.dataSource" + i + ".cachePrepStmts"));
					dataSource.addDataSourceProperty("prepStmtCacheSize", properties.getProperty("mysql.dataSource" + i + ".prepStmtCacheSize"));
					dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", properties.getProperty("mysql.dataSource" + i + ".prepStmtCacheSqlLimit"));
					dataSource.setMaximumPoolSize(Integer.valueOf(properties.getProperty("mysql.dataSource" + i + ".maximumPoolSize")));
					dataSource.setConnectionInitSql(properties.getProperty("mysql.dataSource" + i + ".connectionInitSql", "SET NAMES utf8"));
					checkDataSource(dataSource);
				}

				// mybatis 环境id
				String envId = properties.getProperty("mysql.dataSource" + i + ".env", "development");
				// mybatis mapper所在包路径
				String mapperPackage = properties.getProperty("mysql.dataSource" + i + ".mapperPackage");

				TransactionFactory transactionFactory = new JdbcTransactionFactory();
				Environment environment = new Environment(envId, transactionFactory, dataSource);
				Configuration configuration = new Configuration(environment);
				if(mapperPackage != null && !mapperPackage.isEmpty()) {
					configuration.addMappers(mapperPackage);
				}

				// tk.mybatis 绑定 configuration的mapper
				MapperHelper mapperHelper = new MapperHelper();
				mapperHelper.processConfiguration(configuration);

				// 创建 SqlSessionFactory
				SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
				// put
				sqlSessionMap.put(poolName, sqlSessionFactory);
			}
			initOK = true;
		}
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

	public static SqlSession getSqlSession(String dsName){
		try {
			return sqlSessionMap.get(dsName).openSession(false);
		} catch (Exception e) {
			logger.error("XMySQL.getSqlSession.Exception:", e);
		}
		return null;
	}

	public static void releaseSqlSession(SqlSession sqlSession){
		if (sqlSession != null) {
			try {
				sqlSession.close();
			} catch (Exception e) {
				logger.error("XMySQL.releaseSqlSession.Exception:", e);
			}
		}
	}

}
