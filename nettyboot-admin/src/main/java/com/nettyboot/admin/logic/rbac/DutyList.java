package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.DutyInfoModel;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module= BaseModules.rbac, action="duty_list")
public class DutyList extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		DutyInfoModel dutyModel = new DutyInfoModel();
		JSONArray data = dutyModel.getList();
		if(data == null){
			return this.errorResult();
		}
		return this.successResult(data);
	}
}
