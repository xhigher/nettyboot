package com.nettyboot.admin.logic.staff;

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

@LogicAnnotation(module= BaseModules.staff, action="password_reset", method= LogicMethod.POST, parameters={
		AdminDataKey.staffid,
		BaseDataKey.password})
public final class PasswordReset extends AdminLogic {

	private int targetStaffid = 0;
	private String password = null;
	
	@Override
	protected String prepare() {
		targetStaffid = this.getIntegerParameter(AdminDataKey.staffid);
		password = this.getStringParameter(BaseDataKey.password);
		if(password.length() != 32){
			return errorParameterResult("PASSWORD_ERROR");
		}
		return null;
	}
	
	@Override
	protected String execute() {
		JSONObject staffInfo = null;
		StaffInfoModel staffModel = new StaffInfoModel(targetStaffid);
		
		RedisKey redisKey = AdminRedisKeys.STAFF_INFO.build().append(targetStaffid);
		String redisData = XRedis.get(redisKey);
		if(redisData != null){
			staffInfo = JSONObject.parseObject(redisData);
		}else{
			staffInfo = staffModel.getInfo();
			if(staffInfo == null){
				return errorInternalResult();
			}
			if(staffInfo.isEmpty()){
				return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
			}
			XRedis.set(redisKey, staffInfo.toJSONString());
		}
		if(staffInfo.getInteger(BaseDataKey.status) == StaffInfoModel.Status.blocked){
			return this.errorResult(ErrorCode.ACCOUNT_BLOCKED, "ACCOUNT_BLOCKED");
		}
		
		password = StringUtil.md5(password+staffInfo.getString(BaseDataKey.regtime));
		if(!password.equals(staffInfo.getString(BaseDataKey.password))){
			if(!staffModel.resetPassword(password)){
				return this.errorResult();
			}
			staffInfo = staffModel.getInfo();
			if(staffInfo == null){
				return errorInternalResult();
			}
			if(staffInfo.isEmpty()){
				return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
			}
			XRedis.set(redisKey, staffInfo.toJSONString());
		}
		return this.successResult();
	}
}
