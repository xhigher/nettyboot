package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.base.CommandManager;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.CommandInfoModel;
import com.nettyboot.admin.model.admin.DutyCommandInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module= BaseModules.rbac, action="command_delete", method= LogicMethod.POST, parameters={
		AdminDataKey.cmdid})
public class CommandDelete extends AdminLogic {

	private String cmdid = null;
	@Override
	protected String prepare() {
		cmdid = this.getStringParameter(AdminDataKey.cmdid);
		return null;
	}

	@Override
	protected String execute() {
		CommandInfoModel commandModel = new CommandInfoModel(cmdid);
		JSONObject data = commandModel.getInfo();
		if(data == null){
			return errorInternalResult();
		}
		if(BaseModules.isBaseModule(data.getString(AdminDataKey.module))){
			return this.errorResult("DELETE_FORBIDDEN");
		}
		if(!commandModel.clean()){
			this.errorResult("COMMAND_DELETE_FAILED");
		}
		DutyCommandInfoModel configModel = new DutyCommandInfoModel();
		Boolean hasCommand = configModel.hasCommand(cmdid);
		if(hasCommand == null){
			return this.errorInternalResult();
		}
		if(hasCommand && !configModel.clean(cmdid)){
			this.errorResult("DUTY_COMMAND_DELETE_FAILED");
		}
		return this.successResult();
	}
}
