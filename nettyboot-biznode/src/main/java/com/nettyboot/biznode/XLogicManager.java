package com.nettyboot.biznode;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.RequestInfo;
import com.nettyboot.logic.LogicManager;
import com.nettyboot.rpcmessage.RequestMessageData;
import com.nettyboot.zookeeper.ZooKeeperHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class XLogicManager {

	private static final Logger logger = LoggerFactory.getLogger(XLogicManager.class);

	private static String product;
	private static String business;
	private static String host;
	private static int port;

	private static String zookeeperServers;

	private static ZooKeeperHelper zookeeperHelper;

	private static boolean initOK = false;

    public static void init(Properties properties){
    	product = properties.getProperty("service.product").trim();
    	business = properties.getProperty("service.business").trim();
    	host = properties.getProperty("server.host").trim();
    	port = Integer.parseInt(properties.getProperty("server.port").trim());
		zookeeperServers = properties.getProperty("zookeeper.servers").trim();

		LogicManager.init(properties.getProperty("service.package").trim());

		initZooKeeper();

		initOK = true;

    }

	public static boolean isInitOK(){
		return initOK;
	}
    
    public static String getProduct(){
    	return product;
    }
    
    public static String getBusiness(){
    	return business;
    }

    public static String getHost(){
    	return host;
    }
    
    public static int getPort(){
    	return port;
    }

	public static LogicAnnotation getLogicConfig(String module, String action, int version) {
		return LogicManager.getLogicConfig(module, action, version);
	}
	
	public static String executeLogic(RequestInfo requestInfo){
		return LogicManager.executeLogic(requestInfo);
	}

	private static String getConfigData(){
		JSONObject configData = new JSONObject();
		configData.put("product", product);
		configData.put("business", business);
		configData.put("host", host);
		configData.put("port", port);
		configData.put("logics", LogicManager.getLogicConfigList());
		logger.info("getConfigData: {}", configData.toJSONString());
		return configData.toJSONString();
	}

	private static void initZooKeeper(){
		zookeeperHelper = new ZooKeeperHelper(product, business, zookeeperServers);
		zookeeperHelper.initPublisher(host, port, getConfigData());
	}
}
