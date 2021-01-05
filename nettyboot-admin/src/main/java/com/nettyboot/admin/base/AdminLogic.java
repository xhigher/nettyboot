package com.nettyboot.admin.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.model.admin.StaffRoleInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ErrorCode;
import com.nettyboot.config.RequestInfo;
import com.nettyboot.logic.BaseLogic;
import com.nettyboot.redis.RedisConfig;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

import java.util.HashMap;
import java.util.Map;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public abstract class AdminLogic extends BaseLogic {

	private String xCmdid = null;
	protected int xStaffid = 0;

	protected boolean requireSession(){
		return true;
	}

	protected boolean requireAccountBound(){
		return false;
	}

	@Override
	protected String checkSession(){
		if(this.getSessionid() == null){
			return this.errorResult(ErrorCode.SESSION_INVALID, "SESSION_INVALID");
		}
		RedisKey redisKey1 = AdminRedisKeys.STAFF_SESSIONINFO.build().append(this.getSessionid());
		String redisData = XRedis.get(redisKey1);
		if (redisData == null) {
			return this.errorResult(ErrorCode.SESSION_INVALID, "SESSION_INVALID");
		}
		JSONObject sessionInfo = JSONObject.parseObject(redisData);
		if (sessionInfo == null) {
			return errorInternalResult();
		}
		if(!this.getPeerid().equals(sessionInfo.getString(BaseDataKey.peerid))){
			XRedis.del(redisKey1);
			return this.errorResult(ErrorCode.SESSION_INVALID, "SESSION_INVALID");
		}

		xStaffid = sessionInfo.getInteger(AdminDataKey.staffid);

		if(!"common".equals(this.getModule())){
			RedisKey redisKey2 = AdminRedisKeys.STAFF_COMMANDS.build().append(xStaffid);
			if (XRedis.exists(redisKey2)) {
				redisData = XRedis.hget(redisKey2, xCmdid);
				if (redisData == null) {
					return this.errorResult(ErrorCode.NO_PERMISSION, "NO_PERMISSION");
				}
			} else {
				StaffRoleInfoModel staffRoleModel = new StaffRoleInfoModel(xStaffid);
				JSONArray tempData = staffRoleModel.getStaffCommandList();
				if (tempData == null) {
					return errorInternalResult();
				}
				Map<String, String> commands = new HashMap<String, String>();
				for (int i = 0; i < tempData.size(); i++) {
					commands.put(tempData.getJSONObject(i).getString(AdminDataKey.cmdid), "1");
				}
				XRedis.hmset(redisKey2, commands);
				if (!commands.containsKey(xCmdid)) {
					return this.errorResult(ErrorCode.NO_PERMISSION, "NO_PERMISSION");
				}
			}

			String username = sessionInfo.getString(BaseDataKey.username);

			Long liveTime = XRedis.ttl(redisKey1);
			if(liveTime == null) {
				return this.errorInternalResult();
			}
			if(liveTime > 0 && liveTime < RedisConfig.MIN_5) {
				XRedis.expire(redisKey1);
				XRedis.expire(AdminRedisKeys.STAFF_SESSIONID.build().append(username));
			}

		}
		return null;
	}

}
