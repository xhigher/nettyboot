package com.nettyboot.test.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.logic.DefaultLogic;
import com.nettyboot.test.model.WebsiteKeywordRankModel;
import com.nettyboot.test.model.WebsiteKeywordTaskModel;
import com.nettyboot.util.TimeUtil;

@LogicAnnotation(module="keyword", action = "rank_info", parameters= {"domain","keyword","ymd","type"})
public final class KeywordRankInfo extends DefaultLogic {

	private String domain;
	private String keyword;
	private String ymd;
	private String type;
	
    @Override
    protected String prepare() {
    	domain = this.getStringParameter("domain");
    	keyword = this.getStringParameter("keyword");
    	ymd = this.getStringParameter("ymd");
    	type = this.getStringParameter("type");
        return null;
    }
    
    @Override
    protected boolean requireSession() {
    	return false;
    }
    
    @Override
    protected String execute() {

    	
    	
    	WebsiteKeywordTaskModel keywordTaskModel = new WebsiteKeywordTaskModel();
    	JSONObject keywordTaskInfo = keywordTaskModel.getTaskInfo(domain, keyword, ymd, type);
    	if(keywordTaskInfo == null){
    		return this.errorInternalResult();
    	}
    	String taskid = null;
    	if(keywordTaskInfo.isEmpty()) {
    		String todayYMD = TimeUtil.getTodayYMD();
    		if(todayYMD.equals(ymd)) {
    			taskid = keywordTaskModel.addInfo(domain, keyword, ymd, type);
    			if(taskid == null) {
    				return this.errorInternalResult();
    			}
    			keywordTaskInfo.put("taskid", taskid);
    			keywordTaskInfo.put("status", 0);
    		}else {
    			keywordTaskInfo.put("status", -1);
    		}
    		keywordTaskInfo.put("domain", domain);
    		keywordTaskInfo.put("keyword", keyword);
    		keywordTaskInfo.put("ymd", ymd);
    		keywordTaskInfo.put("type", type);
    	}else {
    		if(keywordTaskInfo.getIntValue("status") == 1) {
    			taskid = keywordTaskInfo.getString("taskid");
    	    	WebsiteKeywordRankModel keywordRankModel = new WebsiteKeywordRankModel();
    	    	JSONArray keywordRankData = keywordRankModel.getRankList(taskid);
    	    	if(keywordRankData == null){
    	    		return this.errorInternalResult();
    	    	}
    	    	keywordTaskInfo.put("data", keywordRankData);
    		}
    	}
    	
        return this.successResult(keywordTaskInfo);
    }
}
