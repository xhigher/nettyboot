package com.nettyboot.admin.model.business;

import com.alibaba.fastjson.JSONArray;
public class AreaInfoModel extends BusinessDatabase {
	
	@Override
	protected String tableName() {
		return "area_info";
	}
	
	public JSONArray getProvinceAreaList(String province){
		return this.field("areaid,name").addWhere("province", province).select();
	}
	public JSONArray getProvinceList(){
		return this.field("areaid,name").addWhere("city", "", WhereType.EQ).select();
	}
	
	public JSONArray getCityList(String province){
		return this.prepare().field("areaid,name").addWhere("province",province).addWhere("city", "", WhereType.NEQ).addWhere("county", "",  WhereType.EQ).select();
	}
	
	public JSONArray getCountyList(String city){
		return this.prepare().field("areaid,name").addWhere("city",city).addWhere("county", "", WhereType.NEQ).addWhere("town", "",  WhereType.EQ).select();
	}
	
	public JSONArray getTownList(String county){
		return this.prepare().field("areaid,name").addWhere("county",county).addWhere("town", "", WhereType.NEQ).select();
	}
}
