package com.nettyboot.mysql_mybatis.xhs;


import com.nettyboot.mysql_mybatis.XModel;

public class SchoolDataDatabase extends XModel {

	@Override
	protected String getDataSourceName() {
		return "school_data";
	}


	public <T> T getMapper(Class<T> mapperClazz){
		return this.getMapperHandlerProxy(mapperClazz);
	}


}
