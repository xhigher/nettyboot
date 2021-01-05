package com.nettyboot.logic;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ClientPeer;
import com.nettyboot.config.ErrorCode;
import com.nettyboot.redis.BaseRedisKeys;
import com.nettyboot.redis.RedisConfig;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;
import com.nettyboot.util.ClientUtil;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public abstract class DefaultLogic extends BaseLogic {

	private JSONObject sessionInfo = null;

	protected String xUserid = null;
	protected String xUsername = null;
	protected Integer xType = null;
	protected String xRegtime = null;

	protected boolean requireSession(){
		return true;
	}

	protected boolean requireAccountBound(){
		return false;
	}

	protected Object getSessionValue(String key){
		if(sessionInfo != null){
			return sessionInfo.get(key);
		}
		return null;
	}

	protected void saveSession(String sessionid, JSONObject sessionInfo){
		if(sessionInfo != null){
			RedisKey redisKey = BaseRedisKeys.SESSION_INFO.build().append(sessionid);
			redisKey.set(sessionInfo.toJSONString());
		}
	}

	protected void cleanSession(String sessionid){
		if(sessionid != null){
			XRedis.del(BaseRedisKeys.SESSION_INFO.build().append(sessionid));
		}
	}

	protected String checkSession(){
		if(this.getSessionid() == null){
			return this.errorSessionResult();
		}
		RedisKey redisKey = BaseRedisKeys.SESSION_INFO.build().append(this.getSessionid());
		String redisData = XRedis.get(redisKey);
		if(redisData == null){
			return this.errorSessionResult();
		}
		sessionInfo = JSONObject.parseObject(redisData);
		if(!this.getPeerid().equals(sessionInfo.getString(BaseDataKey.peerid))){
			XRedis.del(redisKey);
			return this.errorSessionResult();
		}

		xUserid = sessionInfo.getString(BaseDataKey.userid);
		xUsername = sessionInfo.getString(BaseDataKey.username);
		xType = sessionInfo.getInteger(BaseDataKey.type);
		xRegtime = sessionInfo.getString(BaseDataKey.regtime);

		Long liveTime = XRedis.ttl(redisKey);
		if(liveTime == null) {
			return this.errorInternalResult();
		}
		if(liveTime > 0 && liveTime < RedisConfig.DAY_1) {
			XRedis.expire(redisKey);
			ClientPeer clientPeer = ClientUtil.checkPeerid(this.getPeerid());
			XRedis.expire(BaseRedisKeys.SESSIONID.build().append(clientPeer.type().id()).append(clientPeer.flag()).append(xUsername));
		}

		if(requireAccountBound()){
			if(xUsername == null){
				return this.errorResult(ErrorCode.ACCOUNT_UNBOUND, "ACCOUNT_UNBOUND");
			}
		}

		return null;
	}

}
