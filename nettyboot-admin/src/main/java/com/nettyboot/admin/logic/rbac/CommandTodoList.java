package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.base.CommandManager;
import com.nettyboot.admin.conf.AdminConfig;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.CommandInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.logic.LogicManager;

import java.util.HashMap;
import java.util.Map;

@LogicAnnotation(module= BaseModules.rbac, action="command_todo_list")
public class CommandTodoList extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		CommandInfoModel commandModel = new CommandInfoModel();
		JSONArray commandList = commandModel.getList();
		if(commandList == null){
			return this.errorInternalResult();
		}
		
		JSONObject itemInfo = null;
		Map<String, Integer> commandMap = new HashMap<String, Integer>();
		for(int i=0; i<commandList.size(); i++){
			itemInfo = commandList.getJSONObject(i);
			commandMap.put(itemInfo.getString(AdminDataKey.cmdid), 1);
		}

		JSONArray resultData = CommandManager.findTodoCommandList(commandMap);

		return this.successResult(resultData);
	}
}
