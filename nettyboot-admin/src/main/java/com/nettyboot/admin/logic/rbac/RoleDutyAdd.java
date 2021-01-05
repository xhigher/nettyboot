package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.RoleDutyInfoModel;
import com.nettyboot.admin.model.admin.StaffRoleInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.rbac, action="duty_assign", method= LogicMethod.POST, parameters={
		AdminDataKey.roleid,
		AdminDataKey.dutyid})
public class RoleDutyAdd extends AdminLogic {

	private int roleid = 0;
	private int dutyid = 0;
	@Override
	protected String prepare() {
		roleid = this.getIntegerParameter(AdminDataKey.roleid);
		dutyid = this.getIntegerParameter(AdminDataKey.dutyid);
		return null;
	}

	@Override
	protected String execute() {
		RoleDutyInfoModel configModel = new RoleDutyInfoModel(roleid);
		if(!configModel.add(dutyid)){
			return this.errorResult();
		}
		String error = cleanStaffDuty();
		if(error != null){
			return error;
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
