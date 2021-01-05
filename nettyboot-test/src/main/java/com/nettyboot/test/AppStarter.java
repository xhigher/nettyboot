package com.nettyboot.test;

import java.util.Properties;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.logic.LogicManager;
import com.nettyboot.mysql.XMySQL;
import com.nettyboot.redis.XRedis;
import com.nettyboot.server.BaseServer;
import com.nettyboot.starter.BaseStarter;
import com.nettyboot.webserver.WebDefaultServer;

public class AppStarter extends BaseStarter {

	public static void main(String[] args) {
		BaseStarter starter = new AppStarter();
		starter.run();
	}

	@Override
	protected BaseServer createServer(Properties properties) {
		WebDefaultServer server = new WebDefaultServer(properties);
		return server;
	}

	@Override
	protected void init(Properties properties) {
		XRedis.init(properties);
		XMySQL.init(properties);
	}

	@Override
	protected void release() {
		XRedis.close();
	}
}
