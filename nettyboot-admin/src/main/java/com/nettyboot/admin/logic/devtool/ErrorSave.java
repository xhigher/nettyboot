package com.nettyboot.admin.logic.devtool;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.business.ConfigErrorModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module= BaseModules.devtool, action="error_save", method= LogicMethod.POST, parameters={
		AdminDataKey.project,
		AdminDataKey.module,
		AdminDataKey.action,
		AdminDataKey.info,
		AdminDataKey.msg
})
public final class ErrorSave extends AdminLogic {

	private Integer errorid = null;
	private String project = null;
	private String module = null;
	private String action = null;
	private String info = null;
	private String msg = null;
	
	@Override
	protected String prepare() {
		errorid = getIntegerParameter(AdminDataKey.errorid);
		if(errorid == null){
			errorid = 0;
		}
		project = getStringParameter(AdminDataKey.project);
		if(project.isEmpty()){
			return errorParameterResult("PROJECT_ERROR");
		}
		module = getStringParameter(AdminDataKey.module);
		if(module.isEmpty()){
			return errorParameterResult("MODULE_ERROR");
		}
		action = getStringParameter(AdminDataKey.action);
		if(action.isEmpty()){
			return errorParameterResult("ACTION_ERROR");
		}
		info = getStringParameter(AdminDataKey.info);
		if(info.isEmpty()){
			return errorParameterResult("INFO_ERROR");
		}
		msg = getStringParameter(AdminDataKey.msg);
		if(msg.isEmpty()){
			return errorParameterResult("MSG_ERROR");
		}
		return null;
	}

	@Override
	protected String execute() {
		ConfigErrorModel errorModel = new ConfigErrorModel();
		if(errorid > 0){
			if(!errorModel.hasInfo(project, module, action, info)) {
				return errorResult("INFO_NOT_EXISTED");
			}
			
			if(!errorModel.updateInfo(errorid, info, msg)){
				return errorResult();
			}
		} else {
			if(errorModel.hasInfo(project, module, action, info)) {
				return errorResult("INFO_EXISTED");
			}
			
			if(!errorModel.addInfo(project, module, action, info, msg)){
				return errorResult();
			}
		}
		return this.successResult();
	}
}
