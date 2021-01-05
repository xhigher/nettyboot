package com.nettyboot.admin.logic.common;

import com.alibaba.fastjson.JSONArray;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.business.AreaInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.common, action="area_list")
public final class AreaList extends AdminLogic {

	@Override
	protected boolean requireSession() {
		return false;
	}
	
	private String province = null;
	private String city = null;
	private String county = null;
	
	@Override
	protected String prepare() {
		province = getStringParameter(AdminDataKey.province);
		if(province == null){
			province = "";
		}
		city = getStringParameter(AdminDataKey.city);
		if(city == null){
			city = "";
		}
		county = getStringParameter(AdminDataKey.county);
		if(county == null){
			county = "";
		}
		return null;
	}
	
	
	@Override
	protected String execute() {
		RedisKey redisKey = null;
		String redisData = null;
		
		AreaInfoModel areaModel = null;
		JSONArray areaList = null;
		
		if(!county.isEmpty()){
			redisKey = AdminRedisKeys.AREA_LIST.build().append(county);
			redisData = XRedis.get(redisKey);
			if(redisData == null){
				areaModel = new AreaInfoModel();
				areaList = areaModel.getTownList(county);
				if(areaList == null){
					return errorInternalResult();
				}
				if(!areaList.isEmpty()){
					XRedis.set(redisKey, areaList.toJSONString());
				}
			}else{
				areaList = JSONArray.parseArray(redisData);
			}
			return successResult(areaList);
		}
		
		if(!city.isEmpty()){
			redisKey = AdminRedisKeys.AREA_LIST.build().append(city);
			redisData = XRedis.get(redisKey);
			if(redisData == null){
				areaModel = new AreaInfoModel();
				areaList = areaModel.getCountyList(city);
				if(areaList == null){
					return errorInternalResult();
				}
				if(!areaList.isEmpty()){
					XRedis.set(redisKey, areaList.toJSONString());
				}
			}else{
				areaList = JSONArray.parseArray(redisData);
			}
			return successResult(areaList);
		}
		
		if(!province.isEmpty()){
			redisKey = AdminRedisKeys.AREA_LIST.build().append(province);
			redisData = XRedis.get(redisKey);
			if(redisData == null){
				areaModel = new AreaInfoModel();
				areaList = areaModel.getCityList(province);
				if(areaList == null){
					return errorInternalResult();
				}
				if(!areaList.isEmpty()){
					XRedis.set(redisKey, areaList.toJSONString());
				}
			}else{
				areaList = JSONArray.parseArray(redisData);
			}
			return successResult(areaList);
		}
		
		redisKey = AdminRedisKeys.AREA_LIST.build().append("0");
		redisData = XRedis.get(redisKey);
		if(redisData == null){
			areaModel = new AreaInfoModel();
			areaList = areaModel.getProvinceList();
			if(areaList == null){
				return errorInternalResult();
			}
			if(!areaList.isEmpty()){
				XRedis.set(redisKey, areaList.toJSONString());
			}
		}else{
			areaList = JSONArray.parseArray(redisData);
		}
		return this.successResult(areaList);
	}
}
