package com.nettyboot.admin.logic.staff;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminConfig;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.StaffRoleInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.staff, action="auth_role_delete", method= LogicMethod.POST, parameters={
		AdminDataKey.staffid,
		AdminDataKey.roleid})
public class AuthRoleDelete extends AdminLogic {

	private int targetStaffid = 0;
	private int roleid = 0;
	@Override
	protected String prepare() {
		targetStaffid = this.getIntegerParameter(AdminDataKey.staffid);
		roleid = this.getIntegerParameter(AdminDataKey.roleid);
		if(AdminConfig.SUPERMAN_STAFFID == targetStaffid){
			for(int i=0; i<AdminConfig.ROLE_BASE_LIST.length; i++){
				if(roleid == AdminConfig.ROLE_BASE_LIST[i]){
					return errorParameterResult("DELETED_FIRBIDDEN");
				}
			}
		}
		return null;
	}

	@Override
	protected String execute() {
		StaffRoleInfoModel configModel = new StaffRoleInfoModel(targetStaffid);
		if(!configModel.remove(roleid)){
			return this.errorResult();
		}
		XRedis.del(AdminRedisKeys.STAFF_COMMANDS.build().append(targetStaffid));
		XRedis.hdel(AdminRedisKeys.STAFF_MENU.build(), String.valueOf(targetStaffid));
		return this.successResult();
	}
}
