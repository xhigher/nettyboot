package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BranchInfoModel extends AdminDatabase {

	@Override
	protected String tableName() {
		return "branch_info";
	}
	
    public interface Status {
        public static final int activated = 1;
        public static final int blocked = 0;   
	}
    
	public boolean create(int upbranchid, String name, String province, String city, String county, String town, String areas, String remark){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("upbranchid", upbranchid);
		values.put("name", name);
		values.put("province", province);
		values.put("city", city);
		values.put("county", county);
		values.put("town", town);
		values.put("areas", areas);
		values.put("remark", remark);
		values.put("status", Status.activated);
		return this.prepare().set(values).insert();
	}
	
	public JSONObject getInfo(int branchid){
		return this.prepare().addWhere("branchid",branchid).find();
	}

	public JSONArray getSimpleList(){
		return this.prepare().field("branchid,name").addWhere("status", Status.activated).select();
	}
	
	public boolean enable(int branchid){
		return this.prepare().set("status", Status.activated).addWhere("status", Status.blocked).addWhere("branchid", branchid).update();
	}
	
	public boolean disable(int branchid){
		return this.prepare().set("status", Status.blocked).addWhere("status", Status.activated).addWhere("branchid", branchid).update();
	}
	
	public boolean distory(int branchid){
		return this.prepare().addWhere("status", Status.blocked).addWhere("branchid", branchid).delete();
	}
}
