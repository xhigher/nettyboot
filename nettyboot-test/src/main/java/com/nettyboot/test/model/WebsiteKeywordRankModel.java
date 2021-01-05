package com.nettyboot.test.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class WebsiteKeywordRankModel extends BusinessDatabase {

	@Override
	protected String tableName() {
		return "website_keyword_rank";
	}
	
	public JSONArray getRankList(String taskid) {
		return this.prepare().addWhere("taskid", taskid).select();
	}
	
	public boolean saveInfo(String taskid, JSONArray details) {
		List<Map<String, Object>> allValues = new ArrayList<Map<String, Object>>();
		Map<String, Object> values = null;
		JSONObject item = null;
		for(int i=0; i<details.size(); i++) {
			item = details.getJSONObject(i);
			values = new HashMap<String, Object>();
			values.put("taskid", taskid);
			values.put("url", item.getString("url"));
			values.put("rank", item.getString("rank"));
			values.put("ymdhms", item.getString("ymdhms"));
			allValues.add(values);
		}
		return this.prepare().set(allValues).insertBatch();
	}

}
