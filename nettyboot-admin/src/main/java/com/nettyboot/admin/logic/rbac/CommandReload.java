package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.StaffInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

@LogicAnnotation(module= BaseModules.rbac, action="command_reload", method= LogicMethod.POST)
public class CommandReload extends AdminLogic {

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		StaffInfoModel staffModel = new StaffInfoModel();
		JSONArray staffidList = staffModel.getActivatedStaffidList();
		if(staffidList == null){
			return errorInternalResult();
		}
		if(!staffidList.isEmpty()){
			RedisKey[] keys = new RedisKey[staffidList.size()];
			for(int i=0; i<staffidList.size(); i++) {
				keys[i] = AdminRedisKeys.STAFF_COMMANDS.build().append(staffidList.getJSONObject(i).getString(AdminDataKey.staffid));
			}
			XRedis.del(keys);
			XRedis.del(AdminRedisKeys.STAFF_MENU.build());
		}
		
		return this.successResult();
	}
}
