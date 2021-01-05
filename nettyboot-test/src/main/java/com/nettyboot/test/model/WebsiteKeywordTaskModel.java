package com.nettyboot.test.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.util.StringUtil;
import com.nettyboot.util.TimeUtil;

public class WebsiteKeywordTaskModel extends BusinessDatabase {

	@Override
	protected String tableName() {
		return "website_keyword_task";
	}
	
	public JSONObject getTaskInfo(String domain, String keyword, String ymd, String type) {
		return this.prepare().addWhere("domain", domain).addWhere("keyword", keyword).addWhere("ymd", ymd).addWhere("type", type).limit(1).find();
	}
	
	public JSONObject getTaskInfo(String taskid) {
		return this.prepare().addWhere("taskid", taskid).limit(1).find();
	}
	
	public String addInfo(String domain, String keyword, String ymd, String type) {
		String taskid = TimeUtil.getCurrentYMDHMSS2() + StringUtil.randomNumbers(5);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("domain", domain);
		values.put("keyword", keyword);
		values.put("ymd", ymd);
		values.put("type", type);
		values.put("taskid", taskid);
		values.put("status", 0);
		values.put("ymdhms0",TimeUtil.getCurrentYMDHMS());
		values.put("ymdhms1","");
		if(this.prepare().set(values).addWhere("taskid","11111").insertNotExists()) {
			return taskid;
		}
		return null;
	}
	
	public boolean setFinished(String taskid) {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("status", 1);
		values.put("ymdhms1",TimeUtil.getCurrentYMDHMS());
		return this.prepare().set(values).addWhere("status", 0).addWhere("taskid", taskid).update();
	}

}
