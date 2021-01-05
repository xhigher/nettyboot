package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.base.CommandManager;
import com.nettyboot.admin.conf.AdminConfig;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.CommandInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;


@LogicAnnotation(module= BaseModules.rbac, action="command_add", method= LogicMethod.POST, parameters={
		AdminDataKey.module,
		AdminDataKey.action,
		BaseDataKey.name,
		BaseDataKey.type,})
public class CommandAdd extends AdminLogic {

	private String name = null;
	private String module = null;
	private String action = null;
	private Integer type = null;
	
	@Override
	protected String prepare() {
		module = this.getStringParameter(AdminDataKey.module);
		action = this.getStringParameter(AdminDataKey.action);
		name = this.getStringParameter(BaseDataKey.name);
		type = this.getIntegerParameter(BaseDataKey.type);
		if(type == null){
			type = 1;
		}
		if(type != 1 && type != 0){
			type = 1;
		}
		return null;
	}

	@Override
	protected String execute() {
		String cmdid = AdminConfig.getCmdid(module, action);
		if(!CommandManager.hasCommand(cmdid)){
			return this.errorResult("COMMAND_NOT_EXISTED");
		}
		CommandInfoModel commandModel = new CommandInfoModel(cmdid);
		JSONObject data = commandModel.getInfo();
		if(data == null){
			return this.errorInternalResult();
		}
		if(!data.isEmpty()){
			this.errorResult("COMMAND_EXISTED");
		}
		if(!commandModel.add(name, module, action, type)){
			return this.errorResult();
		}
		return this.successResult();
	}
}
