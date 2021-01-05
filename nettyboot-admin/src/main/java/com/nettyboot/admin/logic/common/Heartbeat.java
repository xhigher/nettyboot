package com.nettyboot.admin.logic.common;


import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.config.LogicAnnotation;

@LogicAnnotation(module= BaseModules.common, action="heartbeat")
public class Heartbeat extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		return this.successResult();
	}
}
