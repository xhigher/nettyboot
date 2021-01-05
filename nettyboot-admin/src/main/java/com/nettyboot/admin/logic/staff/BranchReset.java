package com.nettyboot.admin.logic.staff;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.StaffInfoModel;
import com.nettyboot.config.ErrorCode;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.staff, action="branch_reset", method= LogicMethod.POST, parameters={
		AdminDataKey.staffid,
		AdminDataKey.branchid
})
public final class BranchReset extends AdminLogic {

	private int targetStaffid = 0;
	private int branchid = 0;
	
	@Override
	protected String prepare() {
		targetStaffid = this.getIntegerParameter(AdminDataKey.staffid);
		branchid = this.getIntegerParameter(AdminDataKey.branchid);
		return null;
	}
	
	@Override
	protected String execute() {
		JSONObject staffInfo = null;
		StaffInfoModel staffModel = new StaffInfoModel(targetStaffid);
		
		RedisKey redisKey = AdminRedisKeys.STAFF_INFO.build().append(targetStaffid);
		String redisData = XRedis.get(redisKey);
		if(redisData != null){
			staffInfo = JSONObject.parseObject(redisData);
		}else{
			staffInfo = staffModel.getInfo();
			if(staffInfo == null){
				return errorInternalResult();
			}
			if(staffInfo.isEmpty()){
				return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
			}
			XRedis.set(redisKey, staffInfo.toJSONString());
		}
		if(staffInfo.getInteger(AdminDataKey.branchid) != branchid){
			if(!staffModel.resetBranch(branchid)){
				return this.errorResult();
			}
			staffInfo = staffModel.getInfo();
			if(staffInfo == null){
				return errorInternalResult();
			}
			if(staffInfo.isEmpty()){
				return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
			}
			XRedis.set(redisKey, staffInfo.toJSONString());
		}

		return this.successResult();
	}
}
