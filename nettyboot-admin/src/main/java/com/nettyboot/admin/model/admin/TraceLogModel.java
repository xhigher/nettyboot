package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.util.TimeUtil;

import java.util.HashMap;
import java.util.Map;

public class TraceLogModel extends AdminDatabase {
	
	private final int year;
	
	public TraceLogModel() {
		this.year = TimeUtil.getCurrentYear();
	}
	
	@Override
	protected String tableName() {
		return "trace_log_"+year;
	}
	
	public boolean addInfo(String module, String action, String event, String data1, String data2, int staffid){
		String ymdhms = TimeUtil.getCurrentYMDHMS();
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("command", "/"+module+"/"+action);
		values.put("event", event);
		values.put("data1", data1);
		values.put("data2", data2);
		values.put("staffid", staffid);
		values.put("ymdhms", ymdhms);
		return this.prepare().set(values).insert();
	}
	
	public JSONObject getPageData(Integer staffid,String commoand, String event, String startime,String endtime, int pagenum, int pagesize){
		 this.prepare();
		 if(staffid != null && staffid >0){
			 this.addWhere("staffid", staffid);
		 }
		 if(commoand != null){
			 this.addWhere("commoand", commoand);
		 }
		 if(event != null){
			 this.addWhere("event", event);
		 }
		 if(startime != null){
			 this.addWhere("ymdhms",startime, WhereType.GET);
		 }
		 if(endtime != null){
			 this.addWhere("ymdhms",endtime, WhereType.LET);
		 }
		return this.order("ymdhms", true).page(pagenum, pagesize);
	}
	
}
