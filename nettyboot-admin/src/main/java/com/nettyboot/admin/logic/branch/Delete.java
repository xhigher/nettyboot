package com.nettyboot.admin.logic.branch;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.BranchInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ErrorCode;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module = BaseModules.branch, action="delete", method= LogicMethod.POST, parameters={AdminDataKey.branchid})
public class Delete extends AdminLogic {

	private int branchid = 0;
	
	@Override
	protected String prepare() {
		branchid = this.getIntegerParameter(AdminDataKey.branchid);
		return null;
	}

	@Override
	protected String execute() {
		JSONObject branchInfo = null;
		BranchInfoModel model = new BranchInfoModel();
		
		RedisKey redisKey = AdminRedisKeys.BRANCH_INFO.build().append(branchid);
		String redisData = XRedis.get(redisKey);
		if(redisData != null){
			branchInfo = JSONObject.parseObject(redisData);
		}else{
			if(branchInfo == null){
				branchInfo = model.getInfo(branchid);
				if(branchInfo == null){
					return errorInternalResult();
				}
				if(branchInfo.isEmpty()){
					return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
				}
				redisKey = AdminRedisKeys.BRANCH_INFO.build().append(branchid);
				XRedis.set(redisKey, branchInfo.toJSONString());
			}
		}
		if(branchInfo.getInteger(BaseDataKey.status) == BranchInfoModel.Status.blocked){
			if(!model.distory(branchid)){
				return this.errorResult();
			}
			redisKey = AdminRedisKeys.BRANCH_INFO.build().append(branchid);
			XRedis.del(redisKey);
		}
		return this.successResult();
	}
}
