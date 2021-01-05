package com.nettyboot.admin.logic.staff;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.StaffInfoModel;
import com.nettyboot.admin.model.admin.StaffRoleInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LogicAnnotation(module= BaseModules.staff, action="auth_list", parameters={
		BaseDataKey.pagenum,
		BaseDataKey.pagesize})
public class AuthList extends AdminLogic {

	private int pagesize = 20;
	private int pagenum = 1;
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
		JSONArray data = pageData.getJSONArray("data");
		
		List<Object> staffidList = new ArrayList<Object>();
		for(int i=0; i<data.size(); i++){
			staffidList.add(data.getJSONObject(i).getInteger(AdminDataKey.staffid));
		}
		StaffRoleInfoModel configModel = new StaffRoleInfoModel();
		JSONArray staffRoleList = configModel.getStaffRoleList(staffidList);
		Map<Integer, JSONArray> tempData = new HashMap<Integer, JSONArray>();
		JSONObject itemJSON = null;
		Integer staffid = 0;
		for(int i=0; i<staffRoleList.size(); i++){
			itemJSON = staffRoleList.getJSONObject(i);
			staffid = itemJSON.getIntValue(AdminDataKey.staffid);
			if(!tempData.containsKey(staffid)){
				tempData.put(staffid, new JSONArray());
			}
			itemJSON.remove(AdminDataKey.staffid);
			tempData.get(staffid).add(itemJSON);
		}
		for(int i=0; i<data.size(); i++){
			itemJSON = data.getJSONObject(i);
			staffid = itemJSON.getIntValue(AdminDataKey.staffid);
			if(!tempData.containsKey(staffid)){
				itemJSON.put("roles", new JSONArray());
			}else{
				itemJSON.put("roles", tempData.get(staffid));
			}
		}
		return this.successResult(pageData);
	}
}
