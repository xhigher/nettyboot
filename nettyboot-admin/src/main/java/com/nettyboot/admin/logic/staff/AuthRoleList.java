package com.nettyboot.admin.logic.staff;

import com.alibaba.fastjson.JSONArray;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.RoleInfoModel;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module= BaseModules.staff, action="auth_role_list")
public class AuthRoleList extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		RoleInfoModel roleModel = new RoleInfoModel();
		JSONArray data = roleModel.getList();
		if(data == null){
			return this.errorInternalResult();
		}
		return this.successResult(data);
	}
}
