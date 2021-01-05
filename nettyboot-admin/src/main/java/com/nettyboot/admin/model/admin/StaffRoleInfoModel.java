package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffRoleInfoModel extends AdminDatabase {

	private int staffid = 0;

	public StaffRoleInfoModel(){
	}
	
	public StaffRoleInfoModel(int staffid){
		this.staffid = staffid;
	}
	
	@Override
	protected String tableName() {
		return "staff_role_info";
	}
	
	public boolean add(int roleid){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("staffid_roleid", staffid+"_"+roleid);
		values.put("staffid", staffid);
		values.put("roleid", roleid);
		return this.prepare().set(values).insert();
	}
	
	public JSONArray getAllList(){
		return this.prepare().field("staffid,roleid").select();
	}
	
	public JSONArray getList(){
		return this.prepare().field("roleid").addWhere("staffid",staffid).select();
	}

	public boolean remove(int roleid){
		return this.prepare().addWhere("staffid_roleid", staffid+"_"+roleid).delete();
	}
	
	public boolean clean(){
		return this.prepare().addWhere("staffid", staffid).delete();
	}

	public boolean clean(int roleid){
		return this.prepare().addWhere("roleid", roleid).delete();
	}
	
	public JSONArray getStaffRoleList(List<Object> staffidList){
		SQLBuilder sqlBuilder = this.newSQLBuilder();
		sqlBuilder.append("SELECT t1.`staffid` AS `staffid`,t2.`roleid` AS `roleid`,t2.`name` AS `name` ");
		sqlBuilder.append(" FROM `staff_role_info` t1,`role_info` t2 ");
		sqlBuilder.append(" WHERE t1.`roleid`=t2.`roleid` ");
		if(staffidList.size() > 0){
			sqlBuilder.append(" AND t1.`staffid` IN (");
			for(int i=0,n=staffidList.size(); i<n; i++){
				if(i > 0){
					sqlBuilder.append(",");
				}
				sqlBuilder.append("?");
			}	
			sqlBuilder.append(")");
		}
		return this.prepare().selectBySQL(sqlBuilder.toString(), staffidList);	
	}
	
	public JSONArray getStaffCommandList(){
		SQLBuilder sqlBuilder = this.newSQLBuilder();
		sqlBuilder.append("SELECT DISTINCT t3.`cmdid` AS `cmdid` ");
		sqlBuilder.append(" FROM `staff_role_info` t1,`role_duty_info` t2,`duty_command_info` t3");
		sqlBuilder.append(" WHERE t1.`roleid`=t2.`roleid` AND t2.`dutyid`=t3.`dutyid` AND t1.staffid=?");
		return this.prepare().selectBySQL(sqlBuilder.toString(), staffid);	
	}
	
	public JSONArray getDutyList(){
		String sql = "SELECT DISTINCT t2.`dutyid` AS `dutyid` FROM `staff_role_info` t1,`role_duty_info` t2 WHERE t1.`roleid`=t2.`roleid` AND t1.`staffid`=?";
		return this.prepare().selectBySQL(sql, staffid);
	}

	public JSONArray getStaffidList(int roleid){
		return this.prepare().field("staffid").addWhere("roleid", roleid).select(true);
	}

	public Boolean hasRole(int roleid){
		int total = this.addWhere("roleid", roleid).count();
		if(total < 0){
			return null;
		}
		return total > 0;
	}
}
