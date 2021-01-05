package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.DutyInfoModel;
import com.nettyboot.admin.model.admin.MenuInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;

import java.util.HashMap;
import java.util.Map;

@LogicAnnotation(module= BaseModules.rbac, action="menu_list")
public class MenuList extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		JSONObject itemJSON = null;
		DutyInfoModel dutyModel = new DutyInfoModel();
		JSONArray dutyJSONArray = dutyModel.getList();
		if(dutyJSONArray == null){
			return errorInternalResult();
		}
		Map<Integer, String> dutyMap = new HashMap<Integer, String>();
		for(int i=0; i<dutyJSONArray.size(); i++){
			itemJSON = dutyJSONArray.getJSONObject(i);
			dutyMap.put(itemJSON.getIntValue(AdminDataKey.dutyid), itemJSON.getString(BaseDataKey.name));
		}
		
		MenuInfoModel menuModel = new MenuInfoModel();
		JSONArray menuJSONArray = menuModel.getList();
		if(menuJSONArray == null){
			return errorInternalResult();
		}
		String upmenukey = null;
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
		int dutyid = 0;
		for(int i=0; i<menuJSONArray.size(); i++){
			itemJSON = menuJSONArray.getJSONObject(i);
			upmenukey = itemJSON.getString(AdminDataKey.upmenukey);
			if(!upmenukey.isEmpty()){
				if(tempMap.containsKey(upmenukey)){
					dutyid = itemJSON.getIntValue(AdminDataKey.dutyid);
					if(dutyMap.containsKey(dutyid)){
						itemJSON.put("dutyname", dutyMap.get(dutyid));
					}
					tempMap.get(upmenukey).getJSONArray("items").add(itemJSON);
				}
			}
		}
		return this.successResult(menuConfigJSON);
	}
}
