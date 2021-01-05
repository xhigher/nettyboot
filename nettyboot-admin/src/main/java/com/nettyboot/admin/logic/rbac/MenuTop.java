package com.nettyboot.admin.logic.rbac;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.MenuInfoModel;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module= BaseModules.rbac, action="menu_top", parameters={
		AdminDataKey.menuid})
public class MenuTop extends AdminLogic {

	private String menuid = null;
	
	@Override
	protected String prepare() {
		menuid = this.getStringParameter(AdminDataKey.menuid);
		return null;
	}

	@Override
	protected String execute() {
		MenuInfoModel menuModel = new MenuInfoModel();
		if(!menuModel.setTop(menuid)){
			return this.errorResult();
		}
		return this.successResult();
	}
}
