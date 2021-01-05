package com.nettyboot.test.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.logic.DefaultLogic;
import com.nettyboot.test.model.WebsiteKeywordRankModel;
import com.nettyboot.test.model.WebsiteKeywordTaskModel;

@LogicAnnotation(module="keyword", action = "task_info", parameters= {"taskid"})
public final class KeywordTaskInfo extends DefaultLogic {

	private String taskid;
	
    @Override
    protected String prepare() {
    	taskid = this.getStringParameter("taskid");
        return null;
    }
    
    @Override
    protected boolean requireSession() {
    	return false;
    }
    
    @Override
    protected String execute() {

    	WebsiteKeywordTaskModel keywordTaskModel = new WebsiteKeywordTaskModel();
    	JSONObject keywordTaskInfo = keywordTaskModel.getTaskInfo(taskid);
    	if(keywordTaskInfo == null){
    		return this.errorInternalResult();
    	}
    	if(!keywordTaskInfo.isEmpty()) {
    		if(keywordTaskInfo.getIntValue("status") == 1) {
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
