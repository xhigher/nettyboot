package com.nettyboot.admin.logic.branch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.BranchInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module = BaseModules.branch, action="list", parameters={
		BaseDataKey.pagenum,
		BaseDataKey.pagesize})
public final class List extends AdminLogic {

	private int pagenum = 0;
	private int pagesize = 0;
	
	@Override
	protected String prepare() {
		pagenum = this.getIntegerParameter(BaseDataKey.pagenum);
		pagesize = this.getIntegerParameter(BaseDataKey.pagesize);
		return null;
	}
	
	@Override
	protected String execute() {
		BranchInfoModel branchModel = new BranchInfoModel();
		JSONObject pageData = branchModel.page(pagenum, pagesize);
		if(pageData == null){
			return this.errorInternalResult();
		}
		
		RedisKey redisKey = null;
		String redisData = null;
		int upbranchid = 0;
		JSONObject branchInfo = null;
		JSONObject upbranchInfo = null;
		JSONArray branchList = pageData.getJSONArray("data");
		for(int i=0; i<branchList.size(); i++){
			branchInfo = branchList.getJSONObject(i);
			upbranchid = branchInfo.getIntValue("upbranchid");
			if(upbranchid > 0){
				redisKey = AdminRedisKeys.BRANCH_INFO.build().append(upbranchid);
				redisData = XRedis.get(redisKey);
				if(redisData == null){
					upbranchInfo = branchModel.getInfo(upbranchid);
					if(upbranchInfo == null){
						return errorInternalResult();
					}
					if(upbranchInfo.isEmpty()){
						return this.errorResult("BRANCH_NULL");
					}
					redisKey = AdminRedisKeys.BRANCH_INFO.build().append(upbranchid);
					XRedis.set(redisKey, upbranchInfo.toJSONString());
				}else{
					upbranchInfo = JSONObject.parseObject(redisData);
				}
				branchInfo.put("upbranch", upbranchInfo);
			}
		}
		return this.successResult(pageData);
	}
}
