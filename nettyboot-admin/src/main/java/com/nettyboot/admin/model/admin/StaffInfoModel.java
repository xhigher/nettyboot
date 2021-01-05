package com.nettyboot.admin.model.admin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.util.StringUtil;
import com.nettyboot.util.TimeUtil;

import java.util.HashMap;
import java.util.Map;

public class StaffInfoModel extends AdminDatabase {

	private String username = null;
	private int staffid = 0;

	public StaffInfoModel(String username){
		this.username = username;
	}
	
	public StaffInfoModel(int staffid){
		this.staffid = staffid;
	}
	
	public StaffInfoModel(){

	}
	
	@Override
	protected String tableName() {
		return "staff_info";
	}
	
    public interface Status {
        public static final int activated = 1;
        public static final int blocked = 0;   
	}
	
	public boolean create(String realname, int branchid, String email){
		String regtime = TimeUtil.getCurrentYMDHMS();
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("username", username);
		values.put("password", "");
		values.put("realname", realname);
		values.put("faceimg", "");
		values.put("email", email);
		values.put("branchid", branchid);
		values.put("status", Status.blocked);
		values.put("regtime", regtime);
		return this.prepare().set(values).insert();
	}
	
	public JSONObject getInfo(){
		this.prepare().field("staffid,unionid,username,password,realname,faceimg,email,branchid,status,regtime");
		if(this.staffid > 0){
			this.addWhere("staffid", this.staffid);
		}else if(this.username != null){
			this.addWhere("username",this.username);
		}else{
			this.addWhere("staffid", 0);
		}
		return this.find();
	}

	public JSONObject getInfoByUnionid(String unionid){
		return this.prepare().field("staffid,unionid,username,password,realname,faceimg,email,branchid,status,regtime")
				.addWhere("unionid", unionid).find();
	}

	public JSONObject getInfoByUsername(String username){
		return this.prepare().field("staffid,unionid,username,password,realname,faceimg,email,branchid,status,regtime")
				.addWhere("username", username).find();
	}

	public JSONObject getPageList(int pagenum, int pagesize){
		return this.prepare().field("staffid,username,realname,faceimg,email,branchid,status,regtime").order("staffid", false).page(pagenum, pagesize);
	}
	
	public boolean resetPassword(String password){
		return this.prepare().set("password", password).addWhere("staffid", staffid).update();
	}
	public boolean resetBranch(int branchid){
		return this.prepare().set("branchid", branchid).addWhere("staffid", staffid).update();
	}
	
	
	public boolean enable(String password, String regtime){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("password", StringUtil.md5(StringUtil.md5(password)+regtime));
		values.put("status", Status.activated);
		return this.prepare().set(values).addWhere("status", Status.blocked).addWhere("staffid", staffid).update();
	}
	
	public boolean disable(){
		return this.prepare().set("status", Status.blocked).addWhere("status", Status.activated).addWhere("staffid", staffid).update();
	}
	
	public boolean distory(){
		return this.prepare().addWhere("status", Status.blocked).addWhere("staffid", staffid).delete();
	}
	
	public JSONArray getActivatedStaffidList() {
		return this.prepare().field("staffid").set("status", Status.activated).select();
	}

	public boolean bindUnionid(String unionid, String username){
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("unionid", unionid);
		return this.prepare().set(values).addWhere("username", username).update();
	}
	
}
