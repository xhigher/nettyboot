package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MenuInfoModel extends AdminDatabase {

	@Override
	protected String tableName() {
		return "menu_info";
	}
	 
	public boolean addMainItem(String menukey, String name){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("menuid", menukey);
		values.put("upmenukey", "");
		values.put("menukey", menukey);
		values.put("name", name);
		values.put("dutyid", 0);
		values.put("orderno", System.currentTimeMillis());
		return this.prepare().set(values).insert();
	}
	
	public boolean addSubItem(String upmenukey, String menukey, String name, int dutyid){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("menuid", upmenukey+"_"+menukey);
		values.put("upmenukey", upmenukey);
		values.put("menukey", menukey);
		values.put("name", name);
		values.put("dutyid", dutyid);
		values.put("orderno", System.currentTimeMillis());
		return this.prepare().set(values).insert();
	}
	
	public boolean isMainItem(String menukey){
		return this.prepare().addWhere("menuid", menukey).addWhere("menukey", menukey).count()>0;
	}
	
	public JSONObject getInfo(String menuid){
		return this.prepare().field("menuid,menukey,upmenukey,name,dutyid").addWhere("menuid", menuid).find();
	}
	
	public JSONArray getList(){
		return this.prepare().field("menuid,menukey,upmenukey,name,dutyid").order("orderno", false).select();
	}
	
	public boolean cleanInfo(String menuid){
		return this.prepare().addWhere("menuid", menuid).delete();
	}
	
	public boolean cleanInfo(int dutyid){
		return this.prepare().addWhere("dutyid", dutyid).delete();
	}
	
	public boolean setTop(String menuid){
		return this.prepare().set("orderno", System.currentTimeMillis()).addWhere("menuid", menuid).update();
	}

	public Boolean hasDuty(int dutyid){
		int total = this.addWhere("dutyid", dutyid).count();
		if(total < 0){
			return null;
		}
		return total > 0;
	}
}
