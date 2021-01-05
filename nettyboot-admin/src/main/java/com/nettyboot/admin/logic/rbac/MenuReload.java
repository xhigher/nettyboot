package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.MenuInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.XRedis;

import java.util.HashMap;
import java.util.Map;

@LogicAnnotation(module= BaseModules.rbac, action="menu_reload", method= LogicMethod.POST)
public class MenuReload extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		MenuInfoModel menuModel = new MenuInfoModel();
		JSONArray menuJSONArray = menuModel.getList();
		if(menuJSONArray == null){
			return errorInternalResult();
		}
		String upmenukey = null;
		JSONObject itemJSON = null;
		JSONArray menuConfigJSON = new JSONArray();
		Map<String, JSONObject> tempMap = new HashMap<String, JSONObject>();
		for(int i=0; i<menuJSONArray.size(); i++){
			itemJSON = menuJSONArray.getJSONObject(i);
			upmenukey = itemJSON.getString(AdminDataKey.upmenukey);
			if(upmenukey.isEmpty()){
				itemJSON.put("items", new JSONArray());
				tempMap.put(itemJSON.getString(AdminDataKey.menukey), itemJSON);
				menuConfigJSON.add(itemJSON);
			}
		}
		for(int i=0; i<menuJSONArray.size(); i++){
			itemJSON = menuJSONArray.getJSONObject(i);
			upmenukey = itemJSON.getString(AdminDataKey.upmenukey);
			if(!upmenukey.isEmpty()){
				if(tempMap.containsKey(upmenukey)){
					tempMap.get(upmenukey).getJSONArray("items").add(itemJSON);
				}
			}
		}
		XRedis.set(AdminRedisKeys.MENU_CONFIG.build(), menuConfigJSON.toJSONString());
		XRedis.del(AdminRedisKeys.STAFF_MENU.build());

		return this.successResult();
	}
}
