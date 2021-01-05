package com.nettyboot.admin.logic.devtool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.business.ConfigErrorModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.XRedis;
import com.nettyboot.util.StringUtil;

@LogicAnnotation(module= BaseModules.devtool, action="error_publish", method= LogicMethod.POST)
public final class ErrorPublish extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}
	
	private String getErrorKey(JSONObject itemInfo) {
		return itemInfo.getString(AdminDataKey.module)+"@"+itemInfo.getString(AdminDataKey.action)+"@"+itemInfo.getString(AdminDataKey.info);
	}

	@Override
	protected String execute() {
		ConfigErrorModel errorModel = new ConfigErrorModel();
		String project = null;
		JSONObject itemInfo = null;
		JSONArray errorList = null;
		JSONObject configData = null;
		String redisData = null;
		JSONArray projectList = errorModel.getProjectList();
		for(int i=0; i<projectList.size(); i++) {
			project = projectList.getJSONObject(i).getString(AdminDataKey.project);
			
			configData = new JSONObject();
			errorList = errorModel.getList(project);
			for(int j=0; j<errorList.size(); j++) {
				itemInfo = errorList.getJSONObject(j);
				configData.put(getErrorKey(itemInfo), itemInfo.getString(AdminDataKey.msg));
			}
			
			redisData = configData.toJSONString();
			XRedis.set(AdminRedisKeys.CONFIG_ERROR.build().append(project), redisData);
			XRedis.set(AdminRedisKeys.CONFIG_ERROR_CHECKSUM.build().append(project), StringUtil.md5(redisData));
		}
		return this.successResult();
	}
}
