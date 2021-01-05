package com.nettyboot.test.model;

import com.nettyboot.mysql.XModel;

public abstract class BusinessDatabase extends XModel {

	@Override
	protected String getDataSourceName() {
		return "business";
	}
	
}
