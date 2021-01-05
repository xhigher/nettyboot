package com.nettyboot.admin.logic.rbac;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.CommandInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module= BaseModules.rbac, action="command_rename", method= LogicMethod.POST, parameters={
		AdminDataKey.cmdid,
		BaseDataKey.name})
public class CommandRename extends AdminLogic {

	private String cmdid = null;
	private String name = null;
	@Override
	protected String prepare() {
		cmdid = this.getStringParameter(AdminDataKey.cmdid);
		name = this.getStringParameter(BaseDataKey.name);
		return null;
	}

	@Override
	protected String execute() {
		CommandInfoModel commandModel = new CommandInfoModel(cmdid);
		if(!commandModel.updateName(name)){
			return this.errorResult();
		}
		return this.successResult();
	}
}
