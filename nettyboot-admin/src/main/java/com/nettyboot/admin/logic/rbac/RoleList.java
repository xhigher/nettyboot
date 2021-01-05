package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.RoleInfoModel;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module= BaseModules.rbac, action="role_list")
public class RoleList extends AdminLogic {

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
