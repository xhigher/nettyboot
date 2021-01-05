package com.nettyboot.admin.logic.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.business.AreaInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.common, action="area_dict")
public final class AreaDict extends AdminLogic {

	@Override
	protected boolean requireSession() {
		return false;
	}
	
	private String province = null;
	
	@Override
	protected String prepare() {
		province = getStringParameter(AdminDataKey.province);
		return null;
	}
	
	@Override
	protected String execute() {
		JSONObject areaInfo = null;
		JSONObject provinceDict = null;
		String redisData = XRedis.get(AdminRedisKeys.AREA_PROVINCE_DICT.build());
		if(redisData == null){
			AreaInfoModel areaModel = new AreaInfoModel();
			JSONArray areaList = areaModel.getProvinceList();
			if(areaList == null){
				return errorInternalResult();
			}
			provinceDict = new JSONObject();
			for(int i=0; i<areaList.size(); i++){
				areaInfo = areaList.getJSONObject(i);
				provinceDict.put(areaInfo.getString("areaid"), areaInfo.getString("name"));
			}
			XRedis.set(AdminRedisKeys.AREA_PROVINCE_DICT.build(), provinceDict.toJSONString());
		}else{
			provinceDict = JSONObject.parseObject(redisData);
		}
		
		if(province == null || province.isEmpty()){
			return this.successResult(provinceDict);
		}

		if(!provinceDict.containsKey(province)){
			return this.errorResult();
		}
		
		JSONObject areaDict = null;
		RedisKey redisKey = AdminRedisKeys.AREA_DICT.build().append(province);
		redisData = XRedis.get(redisKey);
		if(redisData == null){
			AreaInfoModel areaModel = new AreaInfoModel();
			JSONArray areaList = areaModel.getProvinceAreaList(province);
			if(areaList == null){
				return errorInternalResult();
			}
			areaDict = new JSONObject();
			for(int i=0; i<areaList.size(); i++){
				areaInfo = areaList.getJSONObject(i);
				areaDict.put(areaInfo.getString("areaid"), areaInfo.getString("name"));
			}
			if(!areaList.isEmpty()){
				XRedis.set(redisKey, areaDict.toJSONString());
			}
		}else{
			areaDict = JSONObject.parseObject(redisData);
		}
	
		return this.successResult(areaDict);
	}
}
