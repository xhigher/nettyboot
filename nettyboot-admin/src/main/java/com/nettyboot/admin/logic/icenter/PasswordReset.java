package com.nettyboot.admin.logic.icenter;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.StaffInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ErrorCode;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;
import com.nettyboot.util.StringUtil;

@LogicAnnotation(module= BaseModules.icenter, action="password_reset", method= LogicMethod.POST, parameters={
		AdminDataKey.password_new})
public final class PasswordReset extends AdminLogic {

	private String passwordNew = null;

	@Override
	protected String prepare() {
		passwordNew = getStringParameter(AdminDataKey.password_new);
		if(passwordNew.length() != 32){
			return errorParameterResult("PASSWORD_NEW_ERROR");
		}
		return null;
	}
	
	@Override
	protected String execute() {
		JSONObject staffInfo = null;
		StaffInfoModel staffModel = new StaffInfoModel(xStaffid);
		
		RedisKey redisKey = AdminRedisKeys.STAFF_INFO.build().append(xStaffid);
		String redisData = XRedis.get(redisKey);
		if(redisData == null){
			staffInfo = staffModel.getInfo();
			if(staffInfo == null){
				return errorInternalResult();
			}
			if(staffInfo.isEmpty()){
				return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
			}
			redisKey = AdminRedisKeys.STAFF_INFO.build().append(xStaffid);
			XRedis.set(redisKey, staffInfo.toJSONString());
		}else{
			staffInfo = JSONObject.parseObject(redisData);
		}
		
		passwordNew = StringUtil.md5(passwordNew + staffInfo.getString(BaseDataKey.regtime));
		if(!staffModel.resetPassword(passwordNew)){
			this.errorResult();
		}
		staffInfo = staffModel.getInfo();
		if(staffInfo == null){
			return errorInternalResult();
		}
		if(staffInfo.isEmpty()){
			return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
		}
		redisKey = AdminRedisKeys.STAFF_INFO.build().append(xStaffid);
		XRedis.set(redisKey, staffInfo.toJSONString());
		
		XRedis.del(AdminRedisKeys.STAFF_SESSIONID.build().append(staffInfo.getString(BaseDataKey.username)));
		XRedis.del(AdminRedisKeys.STAFF_SESSIONINFO.build().append(this.getSessionid()));

		return this.successResult();
	}
}
