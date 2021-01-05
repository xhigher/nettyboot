package com.nettyboot.admin.logic.staff;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.StaffInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ErrorCode;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.staff, action="disable", method= LogicMethod.POST, parameters={
		AdminDataKey.staffid})
public class Disable extends AdminLogic {

	private int targetStaffid = 0;
	
	@Override
	protected String prepare() {
		targetStaffid = this.getIntegerParameter(AdminDataKey.staffid);
		return null;
	}

	@Override
	protected String execute() {
		StaffInfoModel staffModel = new StaffInfoModel(targetStaffid);
		JSONObject staffInfo = staffModel.getInfo();
		if (staffInfo == null) {
			return errorInternalResult();
		}
		if (staffInfo.isEmpty()) {
			return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
		}
		if(staffInfo.getInteger(BaseDataKey.status) != StaffInfoModel.Status.blocked){
			if(!staffModel.disable()){
				return this.errorResult();
			}
			XRedis.del(AdminRedisKeys.STAFF_INFO.build().append(targetStaffid));
			XRedis.del(AdminRedisKeys.STAFF_COMMANDS.build().append(targetStaffid));
			XRedis.hdel(AdminRedisKeys.STAFF_MENU.build(), String.valueOf(targetStaffid));
		}

		return this.successResult();
	}
}
