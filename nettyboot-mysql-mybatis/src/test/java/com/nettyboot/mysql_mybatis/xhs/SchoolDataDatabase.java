package com.nettyboot.mysql_mybatis.xhs;


import com.nettyboot.mysql_mybatis.XMybatisModel;

public class SchoolDataDatabase extends XMybatisModel {

	@Override
	protected String getDataSourceName() {
		return "school_data";
	}




}
