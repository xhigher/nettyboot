package com.nettyboot.mysql_mybatis;

import com.nettyboot.mysql.XMySQL;
import com.nettyboot.util.FileUtil;
import com.nettyboot.util.StringUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class XMybatisMySQL {

	private static final Logger logger = LoggerFactory.getLogger(XMybatisMySQL.class);

	private static final Map<String, SqlSessionFactory> sqlSessionFactoryMap = new HashMap<String, SqlSessionFactory>();
	public static final List<String> poolNameList = new ArrayList<>();

	private static boolean initStarted = false;
	private static boolean initOK = false;

	public static void init(Properties properties){
		if(properties.containsKey("mysql.status") && 1==Integer.valueOf(properties.getProperty("mysql.status").trim())) {
			if(initStarted || initOK){
				return;
			}
			initStarted = true;
			// 初始化 mysql连接池
			XMySQL.init(properties);

			int size = Integer.valueOf(properties.getProperty("mysql.dataSource.size").trim());
			for (int i = 1; i <= size; i++) {
				String poolName = properties.getProperty("mysql.dataSource" + i + ".name");
				// 调用 mysql.XMySQL 的连接池，避免重复创建连接池
				DataSource dataSource = XMySQL.getDataSource(poolName);
				if(dataSource == null){
					// 连接池不存在，创建连接池
					dataSource = createDataSource(properties, i);
				}

				// mybatis 环境id
				String envId = properties.getProperty("mysql.dataSource" + i + ".env", "development");
				// mybatis mapper所在包路径
				String mapperPackage = properties.getProperty("mysql.dataSource" + i + ".mapperPackage");
				String mapperPath = properties.getProperty("mysql.dataSource" + i + ".mapperPath");

				// mybatis 配置初始化
				TransactionFactory transactionFactory = new JdbcTransactionFactory();
				Environment environment = new Environment(envId, transactionFactory, dataSource);
				Configuration configuration = new Configuration(environment);
				// 绑定 mapper
				if(mapperPath != null && !mapperPath.isEmpty()){
					parseResourceMapperXmlFiles(mapperPath, configuration);
				}
				if(mapperPackage != null && !mapperPackage.isEmpty()) {
					try {
						configuration.addMappers(mapperPackage);
					} catch (Exception e) {
						logger.warn("configuration.addMappers.mapperPackage: {}, {}", mapperPackage, e.toString());
					}
				}

				// 创建 SqlSessionFactory
				SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
				// SqlSessionFactory 放入map中，便于获取
				sqlSessionFactoryMap.put(poolName, sqlSessionFactory);

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

	private static void parseResourceMapperXmlFiles(String resourceMapperPath, Configuration configuration){
		if(resourceMapperPath != null && !resourceMapperPath.isEmpty()) {
			try {
				String[] mapperPaths = resourceMapperPath.split(",");
				for (String mapperPath: mapperPaths) {
					mapperPath = mapperPath.trim();
					while (mapperPath.startsWith("/")){
						mapperPath = mapperPath.substring(1);
					}
					if(!mapperPath.isEmpty()) {
						List<String> filePathList = FileUtil.getXmlFilesFromPath(mapperPath);
						logger.info("XMybatisMySQL.parseResourceMapperXmlFile: {}", filePathList);
						for (int j = 0; j < filePathList.size(); j++) {
							String filePath = filePathList.get(j);
							try {
								XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(Resources.getResourceAsStream(filePath), configuration, filePath, configuration.getSqlFragments());
								xmlMapperBuilder.parse();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("XMybatisMySQL.parseResourceMapperXmlFiles resourceMapperPath: " + resourceMapperPath, e);
			}
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

	public static SqlSessionFactory getSqlSessionFactory(String dsName){
		try {
			return sqlSessionFactoryMap.get(dsName);
		} catch (Exception e) {
			logger.error("XMySQL.getSqlSessionFactory.Exception:", e);
		}
		return null;
	}

	public static SqlSession getSqlSession(String dsName){
		return getSqlSession(dsName, false);
	}

	public static SqlSession getSqlSession(String dsName, boolean autoCommit){
		try {
			return sqlSessionFactoryMap.get(dsName).openSession(autoCommit);
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
