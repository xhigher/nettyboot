package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminConfig;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.RoleDutyInfoModel;
import com.nettyboot.admin.model.admin.RoleInfoModel;
import com.nettyboot.admin.model.admin.StaffRoleInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.rbac, action="role_delete", method= LogicMethod.POST, parameters={AdminDataKey.roleid})
public class RoleDelete extends AdminLogic {

	private int roleid = 0;
	
	@Override
	protected String prepare() {
		roleid = this.getIntegerParameter(AdminDataKey.roleid);
		for(int i = 0; i< AdminConfig.ROLE_BASE_LIST.length; i++){
			if(roleid == AdminConfig.ROLE_BASE_LIST[i]){
				return errorParameterResult("DELETED_FIRBIDDEN");
			}
		}
		return null;
	}

	@Override
	protected String execute() {
		RoleInfoModel roleModel = new RoleInfoModel(roleid);
		if(!roleModel.clean()){
			return this.errorResult("ROLE_DELETE_FAILED");
		}
		String error = cleanStaffDuty();
		if(error != null){
			return error;
		}
		RoleDutyInfoModel configModel1 = new RoleDutyInfoModel(roleid);
		Boolean hasRole = configModel1.hasRole();
		if(hasRole == null){
			return this.errorInternalResult();
		}
		if(hasRole && !configModel1.clean()){
			return this.errorResult("ROLE_DUTY_DELETE_FAILED");
		}
		StaffRoleInfoModel configModel2 = new StaffRoleInfoModel();
		hasRole = configModel2.hasRole(roleid);
		if(hasRole == null){
			return this.errorInternalResult();
		}
		if(hasRole && !configModel2.clean(roleid)){
			return this.errorResult("STAFF_ROLE_DELETE_FAILED");
		}
		return this.successResult();
	}

	private String cleanStaffDuty(){
		StaffRoleInfoModel configModel = new StaffRoleInfoModel();
		JSONArray staffidList = configModel.getStaffidList(roleid);
		if(staffidList == null){
			logger.error("StaffRoleInfoModel.getStaffidList ERROR");
			return this.errorInternalResult();
		}
		if(!staffidList.isEmpty()){
			int size = staffidList.size();
			RedisKey[] redisKeys = new RedisKey[size];
			String[] staffids = new String[size];

			String staffid = null;
			for (int i = 0; i < size; i++) {
				staffid = staffidList.getString(i);
				redisKeys[i] = AdminRedisKeys.STAFF_COMMANDS.build().append(staffid);
				staffids[i] = staffid;
			}

			XRedis.del(redisKeys);
			XRedis.hdel(AdminRedisKeys.STAFF_MENU.build(), staffids);
		}

		return null;
	}
}
