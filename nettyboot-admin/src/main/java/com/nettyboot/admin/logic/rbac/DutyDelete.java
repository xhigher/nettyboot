package com.nettyboot.admin.logic.rbac;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminConfig;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.DutyCommandInfoModel;
import com.nettyboot.admin.model.admin.DutyInfoModel;
import com.nettyboot.admin.model.admin.MenuInfoModel;
import com.nettyboot.admin.model.admin.RoleDutyInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module= BaseModules.rbac, action="duty_delete", method= LogicMethod.POST, parameters={
		AdminDataKey.dutyid})
public class DutyDelete extends AdminLogic {

	private int dutyid = 0;
	
	@Override
	protected String prepare() {
		dutyid = this.getIntegerParameter(AdminDataKey.dutyid);
		for(int i = 0; i< AdminConfig.DUTY_BASE_LIST.length; i++){
			if(dutyid == AdminConfig.DUTY_BASE_LIST[i]){
				return errorParameterResult("DELETED_FIRBIDDEN");
			}
		}
		return null;
	}

	@Override
	protected String execute() {
		DutyInfoModel dutyModel = new DutyInfoModel(dutyid);
		if(!dutyModel.clean()){
			return this.errorResult("DUTY_DELETE_FAILED");
		}
		DutyCommandInfoModel configModel1 = new DutyCommandInfoModel(dutyid);
		Boolean hasDuty = configModel1.hasDuty();
		if(hasDuty == null){
			return this.errorInternalResult();
		}
		if(hasDuty && !configModel1.clean()){
			return this.errorResult("DUTY_COMMAND_DELETE_FAILED");
		}
		RoleDutyInfoModel configModel2 = new RoleDutyInfoModel();
		hasDuty = configModel2.hasDuty(dutyid);
		if(hasDuty == null){
			return this.errorInternalResult();
		}
		if(hasDuty && !configModel2.clean(dutyid)){
			return this.errorResult("ROLE_DUTY_DELETE_FAILED");
		}
		MenuInfoModel menuModel = new MenuInfoModel();
		hasDuty = menuModel.hasDuty(dutyid);
		if(hasDuty == null){
			return this.errorInternalResult();
		}
		if(hasDuty && !menuModel.cleanInfo(dutyid)){
			return this.errorResult("ROLE_DUTY_DELETE_FAILED");
		}
		return this.successResult();
	}
}
