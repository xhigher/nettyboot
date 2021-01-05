package com.nettyboot.admin.logic.rbac;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.RoleInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module= BaseModules.rbac, action="role_add", method= LogicMethod.POST, parameters={BaseDataKey.name})
public class RoleAdd extends AdminLogic {

	private String name = null;
	
	@Override
	protected String prepare() {
		name = this.getStringParameter(BaseDataKey.name);
		return null;
	}

	@Override
	protected String execute() {
		RoleInfoModel roleModel = new RoleInfoModel();
		if(!roleModel.add(name)){
			return this.errorResult();
		}
		return this.successResult();
	}
}
