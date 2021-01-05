package com.nettyboot.admin.logic.devtool;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.business.ConfigErrorModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module= BaseModules.devtool, action="error_delete", method= LogicMethod.POST)
public final class ErrorDelete extends AdminLogic {

	private Integer errorid = null;
	
	@Override
	protected String prepare() {
		errorid = getIntegerParameter(AdminDataKey.errorid);
		if(errorid == null){
			errorid = 0;
		}
		return null;
	}

	@Override
	protected String execute() {
		ConfigErrorModel errorModel = new ConfigErrorModel();
		JSONObject errInfo = errorModel.getInfo(errorid);
		if(errInfo == null) {
			return errorInternalResult();
		}
		
		if(errInfo.isEmpty()) {
			return errorResult();
		}
		
		if(!errorModel.deleteInfo(errorid)) {
			return errorResult();
		}
		return this.successResult();
	}
}
