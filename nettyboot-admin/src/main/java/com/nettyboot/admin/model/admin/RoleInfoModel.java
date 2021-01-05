package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RoleInfoModel extends AdminDatabase {


	private int roleid = 0;

	public RoleInfoModel(){
	}
	
	public RoleInfoModel(int roleid){
		this.roleid = roleid;
	}
	
	@Override
	protected String tableName() {
		return "role_info";
	}
	 
	public boolean add(String name){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("name", name);
		return this.prepare().set(values).insert();
	}
	
	public JSONObject getInfo(){
		return this.prepare().field("roleid,name").addWhere("roleid",roleid).find();
	}

	public JSONArray getList(){
		return this.prepare().field("roleid,name").select();
	}
	
	public boolean clean(){
		return this.prepare().addWhere("roleid", roleid).delete();
	}
	
}
