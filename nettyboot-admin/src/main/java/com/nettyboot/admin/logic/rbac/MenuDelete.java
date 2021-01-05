package com.nettyboot.admin.logic.rbac;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.MenuInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module= BaseModules.rbac, action="menu_delete", method= LogicMethod.POST, parameters={
		AdminDataKey.menuid})
public class MenuDelete extends AdminLogic {

	private String menuid = null;
	
	@Override
	protected String prepare() {
		menuid = this.getStringParameter(AdminDataKey.menuid);
		return null;
	}

	@Override
	protected String execute() {
		MenuInfoModel menuModel = new MenuInfoModel();
		if(!menuModel.cleanInfo(menuid)){
			return this.errorResult("MENU_DELETE_FAILED");
		}
		return this.successResult();
	}
}
