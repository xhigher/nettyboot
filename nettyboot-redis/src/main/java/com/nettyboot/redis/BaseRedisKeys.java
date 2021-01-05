package com.nettyboot.redis;

import com.nettyboot.redis.RedisConfig;
import com.nettyboot.redis.RedisKeyBuilder;

public interface BaseRedisKeys {

	class BaseNodeKeyBuilder extends RedisKeyBuilder {
		
		private static final String NODE_NAME = "base";

		public BaseNodeKeyBuilder(String prefix, int expireTime){
			super(NODE_NAME, prefix, expireTime);
		}

		public BaseNodeKeyBuilder(String prefix){
			super(NODE_NAME, prefix, RedisConfig.FOREVER);
		}

	}

	RedisKeyBuilder SESSIONID = new BaseNodeKeyBuilder("sessionid", RedisConfig.DAY_30);
	RedisKeyBuilder SESSION_INFO = new BaseNodeKeyBuilder("session_info", RedisConfig.DAY_30);

	RedisKeyBuilder ACCOUNT_INFO = new BaseNodeKeyBuilder("account_info", RedisConfig.DAY_30);
	RedisKeyBuilder USERID = new BaseNodeKeyBuilder("userid", RedisConfig.DAY_30);
	RedisKeyBuilder USER_INFO = new BaseNodeKeyBuilder("user_info", RedisConfig.DAY_30);
	RedisKeyBuilder USER_PUBINFO = new BaseNodeKeyBuilder("user_pubinfo", RedisConfig.DAY_30);

	RedisKeyBuilder USER_ACTIVE_HOUR = new BaseNodeKeyBuilder( "user_active_hour", RedisConfig.HOUR_1);
	RedisKeyBuilder GUEST_ACTIVE_HOUR = new BaseNodeKeyBuilder("guest_active_hour", RedisConfig.HOUR_1);

}
