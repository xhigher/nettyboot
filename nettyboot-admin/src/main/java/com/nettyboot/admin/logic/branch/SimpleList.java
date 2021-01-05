package com.nettyboot.admin.logic.branch;

import com.alibaba.fastjson.JSONArray;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.BranchInfoModel;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module = BaseModules.branch, action="simple_list")
public final class SimpleList extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}
	
	@Override
	protected String execute() {
		BranchInfoModel branchModel = new BranchInfoModel();
		JSONArray branchList = branchModel.getSimpleList();
		if(branchList == null){
			return this.errorInternalResult();
		}
		return this.successResult(branchList);
	}
}
