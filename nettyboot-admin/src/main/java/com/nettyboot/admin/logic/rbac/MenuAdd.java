package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.DutyInfoModel;
import com.nettyboot.admin.model.admin.MenuInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module= BaseModules.rbac, action="menu_add", method= LogicMethod.POST, parameters={
		AdminDataKey.menukey,
		BaseDataKey.name})
public class MenuAdd extends AdminLogic {

	private String upmenukey = null;
	private String menukey = null;
	private int dutyid = 0;
	private String name = null;
	
	
	@Override
	protected String prepare() {
		upmenukey = this.getStringParameter(AdminDataKey.upmenukey);
		menukey = this.getStringParameter(AdminDataKey.menukey);
		name = this.getStringParameter(BaseDataKey.name);
		if(upmenukey!=null && !upmenukey.isEmpty()){
			String dutyidStr = this.getStringParameter(AdminDataKey.dutyid);
			if(dutyidStr==null || dutyidStr.isEmpty()){
				return errorParameterResult("DUTYID_ERROR");
			}
			dutyid = Integer.valueOf(dutyidStr);
			if(dutyid == 0){
				return errorParameterResult("DUTYID_ERROR");
			}
		}
		return null;
	}

	@Override
	protected String execute() {
		MenuInfoModel menuModel = new MenuInfoModel();
		if(upmenukey==null || upmenukey.isEmpty()){
			if(!menuModel.addMainItem(menukey, name)){
				return this.errorResult();
			}
		}else{
			DutyInfoModel dutyModel = new DutyInfoModel(dutyid);
			JSONObject data = dutyModel.getInfo();
			if(data == null){
				return errorInternalResult();
			}
			if(data.isEmpty()){
				return errorParameterResult("DUTYID_ERROR");
			}

			if(!menuModel.isMainItem(upmenukey)){
				return this.errorResult("UPMENU_NOT_EXITED");
			}

			if(!menuModel.addSubItem(upmenukey, menukey, name, dutyid)){
				return this.errorResult();
			}
		}
		return this.successResult();
	}
}
