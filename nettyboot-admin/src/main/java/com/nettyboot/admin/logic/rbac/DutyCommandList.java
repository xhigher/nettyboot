package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.DutyCommandInfoModel;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module= BaseModules.rbac, action="duty_command_list", parameters={AdminDataKey.dutyid})
public class DutyCommandList extends AdminLogic {

	private int dutyid = 0;
	@Override
	protected String prepare() {
		dutyid = this.getIntegerParameter(AdminDataKey.dutyid);
		return null;
	}

	@Override
	protected String execute() {
		DutyCommandInfoModel configModel = new DutyCommandInfoModel(dutyid);
		JSONArray data = configModel.getCommandList();
		if(data == null){
			return this.errorInternalResult();
		}
		return this.successResult(data);
	}
}
