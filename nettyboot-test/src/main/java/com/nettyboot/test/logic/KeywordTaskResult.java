package com.nettyboot.test.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.logic.DefaultLogic;
import com.nettyboot.test.model.WebsiteKeywordRankModel;
import com.nettyboot.test.model.WebsiteKeywordTaskModel;

@LogicAnnotation(module="keyword", action = "task_result", method=LogicMethod.POST, parameters= {"taskid", "data"})
public final class KeywordTaskResult extends DefaultLogic {

	private String taskid;
	private JSONArray data;
	
    @Override
    protected String prepare() {
    	taskid = this.getStringParameter("taskid");
    	data = this.getJSONArrayParameter("data");
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
    		if(keywordTaskInfo.getIntValue("status") == 0) {
    	    	WebsiteKeywordRankModel keywordRankModel = new WebsiteKeywordRankModel();
    	    	if(!keywordRankModel.saveInfo(taskid, data)){
    	    		return this.errorInternalResult();
    	    	}
    	    	
    	    	if(!keywordTaskModel.setFinished(taskid)) {
    	    		return this.errorInternalResult();
    	    	}
    		}
    	}
    	
        return this.successResult();
    }
}
