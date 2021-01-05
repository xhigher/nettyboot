package com.nettyboot.admin.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.conf.AdminConfig;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.config.LogicConfig;
import com.nettyboot.logic.LogicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager {

	private static Logger logger = LoggerFactory.getLogger(CommandManager.class);

	private static final Map<String, Integer> COMMAND_CONFIG = new HashMap<String, Integer>();

	public static void init(){
		LogicConfig logicConfig = null;
		List<LogicConfig> logicConfigList = LogicManager.getLogicConfigList();
		for(int i=0; i<logicConfigList.size(); i++){
			logicConfig = logicConfigList.get(i);
			COMMAND_CONFIG.put(AdminConfig.getCmdid(logicConfig.getModule(), logicConfig.getAction()), 1);
		}

	}

	public static boolean hasCommand(String cmdid) {
		return COMMAND_CONFIG.containsKey(cmdid);
	}

	public static JSONArray findTodoCommandList(Map<String, Integer> commandMap){
		JSONArray resultData = new JSONArray();
		LogicConfig logicConfig = null;
		String cmdid = null;
		JSONObject itemInfo = null;
		List<LogicConfig> logicConfigList = LogicManager.getLogicConfigList();
		for(int i=0; i<logicConfigList.size(); i++){
			logicConfig = logicConfigList.get(i);
			cmdid = AdminConfig.getCmdid(logicConfig.getModule(), logicConfig.getAction());
			if(!commandMap.containsKey(cmdid)){
				itemInfo = new JSONObject();
				itemInfo.put(AdminDataKey.module, logicConfig.getModule());
				itemInfo.put(AdminDataKey.action, logicConfig.getAction());
				resultData.add(itemInfo);
			}
		}
		return resultData;
	}

}
