package com.nettyboot.admin.logic.icenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.MenuInfoModel;
import com.nettyboot.admin.model.admin.StaffRoleInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.redis.XRedis;

import java.util.HashMap;
import java.util.Map;

@LogicAnnotation(module= BaseModules.icenter, action="menu")
public class Menu extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		String fieldStaffid = String.valueOf(xStaffid);
		String redisData = XRedis.hget(AdminRedisKeys.STAFF_MENU.build(), fieldStaffid);
		JSONArray staffMenuJSONArray = null;
		if(redisData == null){
			JSONObject itemJSON = null;
			JSONArray menuConfigJSON = null;
			redisData = XRedis.get(AdminRedisKeys.MENU_CONFIG.build());
			if(redisData == null){
				MenuInfoModel menuModel = new MenuInfoModel();
				JSONArray menuJSONArray = menuModel.getList();
				if(menuJSONArray == null){
					return errorInternalResult();
				}
				String upmenukey = null;
				menuConfigJSON = new JSONArray();
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
				tempMap = null;
			}else{
				menuConfigJSON = JSONArray.parseArray(redisData);
			}

			StaffRoleInfoModel staffRoleModel = new StaffRoleInfoModel(xStaffid);
			JSONArray staffDutyArray = staffRoleModel.getDutyList();
			if(staffDutyArray == null){
				return errorInternalResult();
			}
			if(staffDutyArray.isEmpty()){
				return this.errorResult("NO_AUTHENTICATED");
			}
			Map<Integer, Integer> dutyMap = new HashMap<Integer, Integer>();
			for(int i=0; i<staffDutyArray.size(); i++){
				dutyMap.put(staffDutyArray.getJSONObject(i).getInteger(AdminDataKey.dutyid), 1);
			}
			staffMenuJSONArray = new JSONArray();
			JSONObject subitemJSON = null;
			JSONArray itemArray = null;
			JSONArray itemArray2 = null;
			for(int i=0; i<menuConfigJSON.size(); i++){
				itemJSON = menuConfigJSON.getJSONObject(i);
				itemJSON.remove(AdminDataKey.dutyid);
				//itemJSON.remove(RBACInfo.KEY_UPMENUKEY);
				itemArray = itemJSON.getJSONArray("items");
				itemArray2 = new JSONArray();
				for(Integer dutyid : dutyMap.keySet()){
					for(int j=0; j<itemArray.size(); j++){
						subitemJSON = itemArray.getJSONObject(j);
						if(dutyid.equals(subitemJSON.getInteger(AdminDataKey.dutyid))){
							subitemJSON.remove(AdminDataKey.dutyid);
							//subitemJSON.remove(RBACInfo.KEY_UPMENUKEY);
							itemArray2.add(subitemJSON);
						}
					}
				}
				itemJSON.put("items", itemArray2);
				if(itemArray2.size() > 0){
					staffMenuJSONArray.add(itemJSON);
				}
			}
			XRedis.hset(AdminRedisKeys.STAFF_MENU.build(), fieldStaffid, staffMenuJSONArray.toJSONString());
			menuConfigJSON = null;
			staffDutyArray = null;
		}else{
			staffMenuJSONArray = JSONArray.parseArray(redisData);
		}
		return this.successResult(staffMenuJSONArray);
	}
}
