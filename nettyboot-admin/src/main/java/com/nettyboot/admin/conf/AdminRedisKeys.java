package com.nettyboot.admin.conf;

import com.nettyboot.redis.RedisConfig;
import com.nettyboot.redis.RedisKeyBuilder;

public interface AdminRedisKeys {

	class AdminNodeKeyBuilder extends RedisKeyBuilder {
		
		private static final String NODE_NAME = "admin";

		public AdminNodeKeyBuilder(String prefix, int expireTime){
			super(NODE_NAME, prefix, expireTime);
		}
	}

	class BizNodeKeyBuilder extends RedisKeyBuilder {

		private static final String NODE_NAME = "business";

		public BizNodeKeyBuilder(String prefix, int expireTime){
			super(NODE_NAME, prefix, expireTime);
		}
	}
	
	RedisKeyBuilder CHECKCODE = new AdminNodeKeyBuilder("checkcode", RedisConfig.HOUR_1);

	RedisKeyBuilder STAFF_SESSIONID_PREFIX = new AdminNodeKeyBuilder("staff_sessionid", RedisConfig.HOUR_1);
	RedisKeyBuilder STAFF_SESSIONID = new AdminNodeKeyBuilder("staff_sessionid", RedisConfig.HOUR_1);
	RedisKeyBuilder STAFF_SESSIONINFO = new AdminNodeKeyBuilder("staff_sessioninfo", RedisConfig.HOUR_1);

	RedisKeyBuilder STAFF_INFO = new AdminNodeKeyBuilder("staff_info", 0);
	RedisKeyBuilder STAFF_COMMANDS = new AdminNodeKeyBuilder("staff_commands", 0);
	RedisKeyBuilder STAFF_MENU = new AdminNodeKeyBuilder("staff_menu", 0);

	RedisKeyBuilder BRANCH_INFO = new AdminNodeKeyBuilder("branch_info", 0);
	RedisKeyBuilder MENU_CONFIG = new AdminNodeKeyBuilder("menu_config", 0);



	RedisKeyBuilder CONFIG_INFO = new BizNodeKeyBuilder("config_info", 0);
	RedisKeyBuilder CONFIG_DICT = new BizNodeKeyBuilder("config_dict", 0);
	RedisKeyBuilder CONFIG_INFO_CHECKSUM = new BizNodeKeyBuilder("config_info_checksum", 0);
	RedisKeyBuilder CONFIG_DICT_CHECKSUM = new BizNodeKeyBuilder("config_dict_checksum", 0);

	RedisKeyBuilder CONFIG_ERROR = new BizNodeKeyBuilder("config_error", 0);
	RedisKeyBuilder CONFIG_ERROR_CHECKSUM = new BizNodeKeyBuilder("config_error_checksum", 0);

	RedisKeyBuilder AREA_LIST = new BizNodeKeyBuilder("area_list", 0);
	RedisKeyBuilder AREA_DICT = new BizNodeKeyBuilder("area_dict", 0);
	RedisKeyBuilder AREA_PROVINCE_DICT = new BizNodeKeyBuilder("area_province_dict", 0);
	
}
