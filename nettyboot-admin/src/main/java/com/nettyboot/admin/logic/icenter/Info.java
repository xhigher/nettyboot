package com.nettyboot.admin.logic.icenter;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.StaffInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ErrorCode;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.icenter, action="info")
public final class Info extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}
	
	@Override
	protected String execute() {
		JSONObject staffInfoJSON = null;
		RedisKey redisKey = AdminRedisKeys.STAFF_INFO.build().append(xStaffid);
		String redisDataStr = XRedis.get(redisKey);
		if(redisDataStr == null){
			StaffInfoModel staffModel = new StaffInfoModel(xStaffid);
			staffInfoJSON = staffModel.getInfo();
			if(staffInfoJSON == null){
				return this.errorInternalResult();
			}
			if(staffInfoJSON.isEmpty()){
				return this.errorResult(ErrorCode.INFO_NULL, "ACCOUNT_NULL");
			}
		}else{
			staffInfoJSON = JSONObject.parseObject(redisDataStr);
		}
		staffInfoJSON.remove(BaseDataKey.password);
		return this.successResult(staffInfoJSON);
	}
}
