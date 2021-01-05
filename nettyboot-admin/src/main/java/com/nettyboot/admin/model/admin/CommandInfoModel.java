package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CommandInfoModel extends AdminDatabase {


	private String cmdid = null;

	public CommandInfoModel(){
	}
	
	public CommandInfoModel(String cmdid){
		this.cmdid = cmdid;
	}
	
	@Override
	protected String tableName() {
		return "command_info";
	}
	 
	public boolean add(String name, String module, String action, int type){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("cmdid", cmdid);
		values.put("name", name);
		values.put("module", module);
		values.put("action", action);
		values.put("type", type);
		return this.prepare().set(values).insert();
	}
	
	public boolean updateName(String name){
		return this.prepare().set("name", name).addWhere("cmdid", cmdid).update();
	}
	
	public JSONObject getInfo(){
		return this.prepare().field("cmdid,name,module,action").addWhere("cmdid", cmdid).find();
	}
	
	public JSONArray getList(){
		return this.prepare().field("cmdid,name,module,action").select();
	}
	
	public boolean clean(){
		return this.prepare().addWhere("cmdid", cmdid).delete();
	}
	
	public JSONObject getPageList(int pagenum, int pagesize, int type){
		return this.prepare().addWhere("type", type).order("module", false).page(pagenum, pagesize);
	}
	
}
