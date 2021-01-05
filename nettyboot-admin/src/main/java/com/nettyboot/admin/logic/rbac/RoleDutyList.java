package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.RoleDutyInfoModel;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module= BaseModules.rbac, action="role_duty_list", parameters={AdminDataKey.roleid})
public class RoleDutyList extends AdminLogic {

	private int roleid = 0;
	@Override
	protected String prepare() {
		roleid = this.getIntegerParameter(AdminDataKey.roleid);
		return null;
	}

	@Override
	protected String execute() {
		RoleDutyInfoModel configModel = new RoleDutyInfoModel(roleid);
		JSONArray data = configModel.getDutyList();
		if(data == null){
			return this.errorInternalResult();
		}
		return this.successResult(data);
	}
}
