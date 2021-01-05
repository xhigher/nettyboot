package com.nettyboot.admin.logic.common;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminConfig;
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

@LogicAnnotation(module= BaseModules.common, action="login", method= LogicMethod.POST, parameters={
		BaseDataKey.username,
		BaseDataKey.password,
		BaseDataKey.checkcode})
public final class Login extends AdminLogic {

	@Override
	protected boolean requireSession() {
		return false;
	}
	
	private String username = null;
	private String password = null;
	private String checkcode = null;
	@Override
	protected String prepare() {
		checkcode = this.getStringParameter(BaseDataKey.checkcode).toLowerCase();
		if(checkcode.length() != 4){
			return ("CHECKCODE_ERROR");
		}
		username = this.getStringParameter(BaseDataKey.username);
		if(!AdminConfig.checkUsername(username)){
			return errorParameterResult("USERNAME_ERROR");
		}
		password = this.getStringParameter(BaseDataKey.password);
		if(password.length() != 32){
			return errorParameterResult("PASSWORD_ERROR");
		}
		
		return null;
	}

	@Override
	protected String execute() {
		String peerid = this.getPeerid();
		RedisKey redisKey = AdminRedisKeys.CHECKCODE.build().append(peerid).append(checkcode);
		String redisData = XRedis.get(redisKey);
		if(redisData == null){
			return errorParameterResult("CHECKCODE_ERROR");
		}
		XRedis.del(redisKey);
		
		redisData = null;
		JSONObject sessionInfo = null;
		String staffid = null;
		JSONObject staffInfo = null;
		redisKey = AdminRedisKeys.STAFF_SESSIONID.build().append(username);
		String sessionid = XRedis.get(redisKey);
		if(sessionid != null){
			redisKey = AdminRedisKeys.STAFF_SESSIONINFO.build().append(sessionid);
			redisData = XRedis.get(redisKey);
			if(redisData != null){
				sessionInfo = JSONObject.parseObject(redisData);
				if(!peerid.equals(sessionInfo.getString(BaseDataKey.peerid))){
					sessionInfo = null;
					XRedis.del(AdminRedisKeys.STAFF_SESSIONID.build().append(username));
					XRedis.del(AdminRedisKeys.STAFF_SESSIONINFO.build().append(sessionid));
				}else{
					XRedis.del(AdminRedisKeys.STAFF_SESSIONID.build().append(username));
				}
			}else{
				XRedis.del(AdminRedisKeys.STAFF_SESSIONID.build().append(username));
			}
		}
		if(sessionInfo != null){
			staffid = sessionInfo.getString(AdminDataKey.staffid);
			redisKey = AdminRedisKeys.STAFF_INFO.build().append(staffid);
			redisData = XRedis.get(redisKey);
			if(redisData != null){
				staffInfo = JSONObject.parseObject(redisData);
			}
		}

		if(staffInfo == null){
			StaffInfoModel staffModel = new StaffInfoModel(username);
			staffInfo = staffModel.getInfo();
			if(staffInfo == null){
				return errorInternalResult();
			}
			if(staffInfo.isEmpty()){
				return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
			}
			staffid = staffInfo.getString(AdminDataKey.staffid);
			redisKey = AdminRedisKeys.STAFF_INFO.build().append(staffid);
			XRedis.set(redisKey, staffInfo.toJSONString());
		}
		
		password = StringUtil.md5(password+staffInfo.getString(BaseDataKey.regtime));
		if(!password.equals(staffInfo.getString(BaseDataKey.password))){
			return this.errorResult(ErrorCode.PASSWORD_ERROR, "PASSWORD_ERROR");
		}
		
		if(staffInfo.getInteger(BaseDataKey.status) == StaffInfoModel.Status.blocked){
			return this.errorResult(ErrorCode.ACCOUNT_BLOCKED, "ACCOUNT_BLOCKED");
		}
		
		
		if(sessionInfo == null){
			sessionInfo = new JSONObject();
			sessionInfo.put(BaseDataKey.peerid, peerid);
			sessionInfo.put(AdminDataKey.staffid, staffid);
			sessionInfo.put(BaseDataKey.username, username);
		}
		sessionInfo.put(BaseDataKey.peerid, peerid);
		
		sessionid = AdminConfig.createSessionid();
		redisKey = AdminRedisKeys.STAFF_SESSIONID.build().append(username);
		XRedis.set(redisKey, sessionid);

		redisKey = AdminRedisKeys.STAFF_SESSIONINFO.build().append(sessionid);
		XRedis.set(redisKey, sessionInfo.toJSONString());

		JSONObject resultJSON = new JSONObject();
		resultJSON.put(BaseDataKey.username, username);
		resultJSON.put(BaseDataKey.sessionid, sessionid);
		return this.successResult(resultJSON);
	}
}
