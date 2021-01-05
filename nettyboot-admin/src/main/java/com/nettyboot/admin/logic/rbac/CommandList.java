package com.nettyboot.admin.logic.rbac;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.CommandInfoModel;
import com.nettyboot.admin.model.admin.DutyCommandInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.LogicAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LogicAnnotation(module= BaseModules.rbac, action="command_list", parameters={
		BaseDataKey.pagenum,
		BaseDataKey.pagesize,
		BaseDataKey.type})
public class CommandList extends AdminLogic {

	private int pagesize = 20;
	private int pagenum = 1;
	
	private int type = 1;
	
	@Override
	protected String prepare() {
		pagenum = this.getIntegerParameter(BaseDataKey.pagenum);
		pagesize = this.getIntegerParameter(BaseDataKey.pagesize);
		
		type = this.getIntegerParameter(BaseDataKey.type);
		return null;
	}

	@Override
	protected String execute() {
		CommandInfoModel commandModel = new CommandInfoModel();
		JSONObject pageData = commandModel.getPageList(pagenum, pagesize, type);
		if(pageData == null){
			return this.errorInternalResult();
		}
		JSONArray data = pageData.getJSONArray("data");
		List<Object> cmdidList = new ArrayList<Object>();
		for(int i=0; i<data.size(); i++){
			cmdidList.add(data.getJSONObject(i).getString(AdminDataKey.cmdid));
		}
		DutyCommandInfoModel configModel = new DutyCommandInfoModel();
		JSONArray dutyList = configModel.getDutyList(cmdidList);
		Map<String, JSONObject> tempData = new HashMap<String, JSONObject>();
		JSONObject itemJSON = null;
		String cmdid = null;
		for(int i=0; i<dutyList.size(); i++){
			itemJSON = dutyList.getJSONObject(i);
			cmdid = itemJSON.getString(AdminDataKey.cmdid);
			itemJSON.remove(AdminDataKey.cmdid);
			tempData.put(cmdid, itemJSON);
		}
		for(int i=0; i<data.size(); i++){
			itemJSON = data.getJSONObject(i);
			cmdid = itemJSON.getString(AdminDataKey.cmdid);
			if(tempData.containsKey(cmdid)){
				itemJSON.put("dutyid", tempData.get(cmdid).getString(AdminDataKey.dutyid));
				itemJSON.put("dutyname", tempData.get(cmdid).getString(BaseDataKey.name));
			}
			if(BaseModules.isBaseModule(itemJSON.getString(AdminDataKey.module))){
				itemJSON.put("deleted", 0);
			}else{
				itemJSON.put("deleted", 1);
			}
		}
		return this.successResult(pageData);
	}
}
