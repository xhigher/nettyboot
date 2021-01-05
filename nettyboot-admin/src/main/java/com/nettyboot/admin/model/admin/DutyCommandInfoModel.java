package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DutyCommandInfoModel extends AdminDatabase {


	private int dutyid = 0;

	public DutyCommandInfoModel(){
	}
	
	public DutyCommandInfoModel(int dutyid){
		this.dutyid = dutyid;
	}
	
	@Override
	protected String tableName() {
		return "duty_command_info";
	}
	 
	public boolean add(String cmdid){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("dutyid_cmdid", dutyid+"_"+cmdid);
		values.put("dutyid", dutyid);
		values.put("cmdid", cmdid);
		return this.prepare().set(values).insert();
	}
	
	public JSONArray getAllList(){
		return this.prepare().field("dutyid,cmdid").select();
	}
	
	public JSONArray getList(){
		return this.prepare().field("dutyid,cmdid").addWhere("dutyid", dutyid).select();
	}
	
	public JSONArray getDutyList(List<Object> cmdidList){
		SQLBuilder sqlBuilder = this.newSQLBuilder();
		sqlBuilder.append("SELECT t1.`cmdid` AS `cmdid`,t2.`dutyid` AS `dutyid`,t2.`name` AS `name` ");
		sqlBuilder.append(" FROM `duty_command_info` t1,`duty_info` t2 ");
		sqlBuilder.append(" WHERE t1.`dutyid`=t2.`dutyid` ");
		if(cmdidList.size() > 0){
			sqlBuilder.append(" AND t1.`cmdid` IN (");
			for(int i=0,n=cmdidList.size(); i<n; i++){
				if(i > 0){
					sqlBuilder.append(",");
				}
				sqlBuilder.append("?");
			}	
			sqlBuilder.append(")");
		}
		return this.prepare().selectBySQL(sqlBuilder.toString(), cmdidList);	
	}
	
	public JSONArray getCommandList(){
		SQLBuilder sqlBuilder = this.newSQLBuilder();
		sqlBuilder.append("SELECT t2.`cmdid` AS `cmdid`,t2.`name` AS `name`,t2.`module` AS `module`,t2.`action` AS `action` ");
		sqlBuilder.append(" FROM `duty_command_info` t1,`command_info` t2 ");
		sqlBuilder.append(" WHERE t1.`cmdid`=t2.`cmdid` AND t1.`dutyid`=?"); 
		return this.prepare().selectBySQL(sqlBuilder.toString(), this.dutyid);	
	}
	
	public boolean remove(String cmdid){
		return this.prepare().addWhere("dutyid_cmdid", dutyid+"_"+cmdid).delete();
	}
	
	public boolean clean(){
		return this.prepare().addWhere("dutyid", dutyid).delete();
	}
	
	public boolean clean(String cmdid){
		return this.prepare().addWhere("cmdid", cmdid).delete();
	}

	public Boolean hasCommand(String cmdid){
		int total = this.addWhere("cmdid", cmdid).count();
		if(total < 0){
			return null;
		}
		return total > 0;
	}

	public Boolean hasDuty(){
		int total = this.addWhere("dutyid", dutyid).count();
		if(total < 0){
			return null;
		}
		return total > 0;
	}
}
