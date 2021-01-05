package com.nettyboot.admin.base;

import com.nettyboot.logic.LogicManager;
import com.nettyboot.webserver.WebBaseHandler;
import com.nettyboot.webserver.WebBaseServer;
import com.nettyboot.webserver.WebDefaultHandler;

import java.util.Properties;

/*
 * @copyright (c) xhigher 2015
 * @author xhigher    2015-3-26 
 */
public class WebAdminServer extends WebBaseServer {

	public WebAdminServer(Properties properties) {
		super(properties);
	}

	@Override
	protected void init(Properties properties){
		LogicManager.init(properties.getProperty("logic.package").trim());
		LogicManager.initLogics("com.nettyboot.admin.logic");
		CommandManager.init();
	}

	@Override
	protected WebBaseHandler newHandler(){
		return new WebDefaultHandler();
	}

}
