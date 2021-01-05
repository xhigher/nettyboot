package com.nettyboot.admin.logic.staff;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminConfig;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.StaffInfoModel;
import com.nettyboot.admin.model.admin.StaffRoleInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.staff, action="create", method= LogicMethod.POST, parameters={
		BaseDataKey.username,
		BaseDataKey.realname,
		AdminDataKey.branchid,
		BaseDataKey.email})
public class Create extends AdminLogic {

	private String username = null;
	private String realname = null;
	private int branchid = 0;
	private String email = null;
	
	@Override
	protected String prepare() {
		username = this.getStringParameter(BaseDataKey.username);
		if(!AdminConfig.checkUsername(username)){
			return errorParameterResult("USERNAME_ERROR");
		}
		realname = this.getStringParameter(BaseDataKey.realname);
		branchid = this.getIntegerParameter(AdminDataKey.branchid);
		email = this.getStringParameter(BaseDataKey.email);
		return null;
	}

	@Override
	protected String execute() {
		StaffInfoModel staffModel = new StaffInfoModel(username);
		JSONObject staffInfo = staffModel.getInfo();
		if(staffInfo == null){
			return this.errorInternalResult();
		}
		if(!staffInfo.isEmpty()){
			return this.errorResult("STAFF_EXISTED");
		}
		
		if(!staffModel.create(realname, branchid, email)){
			return this.errorResult();
		}
		staffInfo = staffModel.getInfo();
		if(staffInfo == null){
			return this.errorInternalResult();
		}
		if(staffInfo.isEmpty()){
			return this.errorResult("STAFF_NULL");
		}
		int targetStaffid = staffInfo.getIntValue(AdminDataKey.staffid);
		XRedis.set(AdminRedisKeys.STAFF_INFO.build().append(targetStaffid), staffInfo.toJSONString());
		
		StaffRoleInfoModel staffRoleModel = new StaffRoleInfoModel(targetStaffid);
		if(!staffRoleModel.add(AdminConfig.ROLEID_USER)){
			return this.errorResult("RBAC_FAILED");
		}

		staffInfo.remove(BaseDataKey.password);
		return this.successResult(staffInfo);
	}
}
