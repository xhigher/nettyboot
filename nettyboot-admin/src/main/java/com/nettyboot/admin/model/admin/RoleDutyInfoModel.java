package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class RoleDutyInfoModel extends AdminDatabase {


	private int roleid = 0;

	public RoleDutyInfoModel(){
	}
	
	public RoleDutyInfoModel(int roleid){
		this.roleid = roleid;
	}

	@Override
	protected String tableName() {
		return "role_duty_info";
	}
	 
	public boolean add(int dutyid){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("roleid_dutyid", roleid+"_"+dutyid);
		values.put("roleid", roleid);
		values.put("dutyid", dutyid);
		return this.prepare().set(values).insert();
	}
	
	public JSONArray getAllList(){
		return this.prepare().field("roleid,dutyid").select();
	}
	
	public JSONArray getList(){
		return this.prepare().field("roleid,dutyid").addWhere("roleid", roleid).select();
	}
	
	public JSONArray getDutyList(){
		SQLBuilder sqlBuilder = this.newSQLBuilder();
		sqlBuilder.append("SELECT t2.`dutyid` AS `dutyid`,t2.`name` AS `name` ");
		sqlBuilder.append(" FROM `role_duty_info` t1,`duty_info` t2 ");
		sqlBuilder.append(" WHERE t1.`dutyid`=t2.`dutyid` AND t1.`roleid`=?");
		return this.prepare().selectBySQL(sqlBuilder.toString(), this.roleid);	
	}
	
	public boolean remove(int dutyid){
		return this.prepare().addWhere("roleid_dutyid", roleid+"_"+dutyid).delete();
	}
	
	public boolean clean(){
		return this.prepare().addWhere("roleid", roleid).delete();
	}
	
	public boolean clean(int dutyid){
		return this.prepare().addWhere("dutyid", dutyid).delete();
	}

	public Boolean hasRole(){
		int total = this.addWhere("roleid", roleid).count();
		if(total < 0){
			return null;
		}
		return total > 0;
	}

	public Boolean hasDuty(int dutyid){
		int total = this.addWhere("dutyid", dutyid).count();
		if(total < 0){
			return null;
		}
		return total > 0;
	}
	
}
