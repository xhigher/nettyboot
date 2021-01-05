package com.nettyboot.admin.logic.devtool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.business.ConfigErrorModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module= BaseModules.devtool, action="error_list")
public final class ErrorList extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		ConfigErrorModel errorModel = new ConfigErrorModel();
		
		JSONArray projectList = errorModel.getProjectList();
		if(projectList == null){
			return errorInternalResult();
		}
		
		JSONObject projectInfo = null;
		JSONArray moduleList = null;
		JSONObject moduleInfo = null;
		JSONArray actionList = null;
		JSONObject actionInfo = null;
		String project = null;
		String module = null;
		String action = null;
		JSONArray list = null;
		
		for(int i=0; i<projectList.size(); i++){
			projectInfo = projectList.getJSONObject(i);
			project = projectInfo.getString("project");
			
			moduleList = errorModel.getModuleList(project);
			if(moduleList == null){
				return errorInternalResult();
			}
			projectInfo.put("items", moduleList);
			projectInfo.put("info", project);
			
			for(int j=0; j<moduleList.size(); j++){
				moduleInfo = moduleList.getJSONObject(j);
				module = moduleInfo.getString(AdminDataKey.module);
				
				actionList = errorModel.getActionList(project, module);
				if(actionList == null){
					return errorInternalResult();
				}
				moduleInfo.put("items", actionList);
				moduleInfo.put("info", module);
				moduleInfo.put("project", project);
				
				for(int k=0; k<actionList.size(); k++){
					actionInfo = actionList.getJSONObject(k);
					action = actionInfo.getString(AdminDataKey.action);
					
					list = errorModel.getList(project, module, action);
					if(list == null){
						return errorInternalResult();
					}
					actionInfo.put("items", list);
					actionInfo.put("info", action);
					actionInfo.put("action", action);
					actionInfo.put("project", project);
					actionInfo.put("module", module);
				}
			}
		}
		
		return this.successResult(projectList);
	}
}
