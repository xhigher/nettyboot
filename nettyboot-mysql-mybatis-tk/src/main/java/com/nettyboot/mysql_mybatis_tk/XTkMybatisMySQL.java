package com.nettyboot.mysql_mybatis_tk;

import com.nettyboot.mysql_mybatis.XMybatisMySQL;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.mybatis.mapper.mapperhelper.MapperHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XTkMybatisMySQL {

	private static final Logger logger = LoggerFactory.getLogger(XTkMybatisMySQL.class);

	private static final Map<String, SqlSessionFactory> sqlSessionFactoryMap = new HashMap<String, SqlSessionFactory>();

	private static boolean initStarted = false;
	private static boolean initOK = false;

	public static void init(Properties properties){

		if(properties.containsKey("mysql.status") && 1==Integer.valueOf(properties.getProperty("mysql.status").trim())) {
			if (initStarted || initOK) {
				return;
			}
			initStarted = true;
			// 初始化 mysql Mybatis 配置
			XMybatisMySQL.init(properties);

			for (int i = 0; i < XMybatisMySQL.poolNameList.size(); i++) {
				String poolName = XMybatisMySQL.poolNameList.get(i);

				SqlSessionFactory sqlSessionFactory = XMybatisMySQL.getSqlSessionFactory(poolName);
				if (sqlSessionFactory != null) {
					// tk.mybatis 绑定 configuration的mapper
					MapperHelper mapperHelper = new MapperHelper();
					mapperHelper.processConfiguration(sqlSessionFactory.getConfiguration());
				}
			}
			initOK = true;
		}
	}

}
