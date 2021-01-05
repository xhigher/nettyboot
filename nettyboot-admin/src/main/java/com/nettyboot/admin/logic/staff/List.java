package com.nettyboot.admin.logic.staff;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.BranchInfoModel;
import com.nettyboot.admin.model.admin.StaffInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.staff, action="list", parameters={
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
		StaffInfoModel staffModel = new StaffInfoModel();
		JSONObject pageData = staffModel.getPageList(pagenum, pagesize);
		if(pageData == null){
			return this.errorInternalResult();
		}
		
		RedisKey redisKey = null;
		String redisData = null;
		int branchid = 0;
		JSONObject staffInfo = null;
		JSONObject branchInfo = null;
		BranchInfoModel branchModel = new BranchInfoModel();
		JSONArray staffList = pageData.getJSONArray("data");
		for(int i=0; i<staffList.size(); i++){
			staffInfo = staffList.getJSONObject(i);
			branchid = staffInfo.getIntValue("branchid");
			if(branchid > 0){
				redisKey = AdminRedisKeys.BRANCH_INFO.build().append(branchid);
				redisData = XRedis.get(redisKey);
				if(redisData == null){
					branchInfo = branchModel.getInfo(branchid);
					if(branchInfo == null){
						return errorInternalResult();
					}
					if(branchInfo.isEmpty()){
						return this.errorResult("BRANCH_NULL");
					}
					XRedis.set(redisKey, branchInfo.toJSONString());
				}else{
					branchInfo = JSONObject.parseObject(redisData);
				}
				staffInfo.put("branch", branchInfo);
			}
		}
		return this.successResult(pageData);
	}
}
