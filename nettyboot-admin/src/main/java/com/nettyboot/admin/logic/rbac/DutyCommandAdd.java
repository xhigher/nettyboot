package com.nettyboot.admin.logic.rbac;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.DutyCommandInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module= BaseModules.rbac, action="duty_command_add", method= LogicMethod.POST, parameters={
		AdminDataKey.dutyid,
		AdminDataKey.cmdid})
public class DutyCommandAdd extends AdminLogic {

	private String cmdid = null;
	private int dutyid = 0;
	@Override
	protected String prepare() {
		cmdid = this.getStringParameter(AdminDataKey.cmdid);
		dutyid = this.getIntegerParameter(AdminDataKey.dutyid);
		return null;
	}

	@Override
	protected String execute() {
		DutyCommandInfoModel configModel = new DutyCommandInfoModel(dutyid);
		if(!configModel.add(cmdid)){
			return this.errorResult();
		}
		return this.successResult();
	}
}
