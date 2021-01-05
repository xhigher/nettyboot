package com.nettyboot.admin.model.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ConfigErrorModel extends BusinessDatabase {

	@Override
	protected String tableName() {
		return "config_error";
	}

	public boolean addInfo(String project, String module, String action, String info, String msg) {
		Map<String,Object> values = new HashMap<String,Object>();
		values.put("project", project);
		values.put("module", module);
		values.put("action", action);
		values.put("info", info);
		values.put("msg", msg);
		return this.prepare().set(values).insert();
	}
	
	public boolean updateInfo(int errorid, String info, String msg) {
		Map<String,Object> values = new HashMap<String,Object>();
		values.put("info", info);
		values.put("msg", msg);
		return this.prepare().set(values).addWhere("errorid", errorid).update();
	}
	
	public JSONObject getPageList(int pagenum, int pagesize) {
		return this.prepare().page(pagenum, pagesize);
	}
	
	public JSONObject getInfo(int errorid) {
		return this.prepare().addWhere("errorid", errorid).find();
	}
	
	public boolean hasInfo(String project, String module, String action, String info){
		return this.prepare().addWhere("project", project).addWhere("module", module).addWhere("action", action).addWhere("info", info).count() > 0;
	}
	
	public JSONArray getProjectList(){
		return this.prepare().field("project").group("project").select();
	}
	
	public JSONArray getModuleList(String project){
		return this.prepare().field("project,module").addWhere("project", project).group("module").select();
	}
	
	public JSONArray getActionList(String project, String module){
		return this.prepare().field("project,module,action").addWhere("module", module).addWhere("project", project).group("action").select();
	}
	
	public JSONArray getList(String project, String module, String action){
		return this.prepare().addWhere("project", project).addWhere("module", module).addWhere("action", action).select();
	}
	
	public JSONArray getList(){
		return this.prepare().select();
	}
	
	public boolean deleteInfo(int errorid) {
		return this.prepare().addWhere("errorid", errorid).delete();
	}
	
	public JSONArray getList(String project){
		return this.prepare().addWhere("project", project).select();
	}
}
