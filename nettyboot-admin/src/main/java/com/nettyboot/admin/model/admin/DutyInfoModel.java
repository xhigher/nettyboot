package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DutyInfoModel extends AdminDatabase {


	private int dutyid = 0;

	public DutyInfoModel(){
	}
	
	public DutyInfoModel(int dutyid){
		this.dutyid = dutyid;
	}
	
	@Override
	protected String tableName() {
		return "duty_info";
	}
	 
	public boolean add(String name){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("name", name);
		return this.prepare().set(values).insert();
	}
	
	public JSONObject getInfo(){
		return this.prepare().field("dutyid,name").addWhere("dutyid",dutyid).find();
	}

	public JSONArray getList(){
		return this.prepare().field("dutyid,name").select();
	}
	
	public boolean clean(){
		return this.prepare().addWhere("dutyid", dutyid).delete();
	}
	
}
