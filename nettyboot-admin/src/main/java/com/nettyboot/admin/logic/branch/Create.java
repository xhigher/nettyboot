package com.nettyboot.admin.logic.branch;

import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.BranchInfoModel;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;

@LogicAnnotation(module = BaseModules.branch, action="create", method= LogicMethod.POST,
		parameters={
				AdminDataKey.upbranchid,
				AdminDataKey.name,
				AdminDataKey.province,
				AdminDataKey.city,
				AdminDataKey.county,
				AdminDataKey.town,
				AdminDataKey.areas,
				AdminDataKey.remark,
		})
public class Create extends AdminLogic {

	private int upbranchid = 0;
	private String name = null;
	private String province = null;
	private String city = null;
	private String county = null;
	private String town = null;
	private String areas = null;
	private String remark = null;
	
	@Override
	protected String prepare() {
		upbranchid = getIntegerParameter(AdminDataKey.upbranchid);
		name = getStringParameter(AdminDataKey.name);
		
		province = getStringParameter(AdminDataKey.province);
		if(province == null){
			province = "";
		}
		city = getStringParameter(AdminDataKey.city);
		if(city == null){
			city = "";
		}
		county = getStringParameter(AdminDataKey.county);
		if(county == null){
			county = "";
		}
		town = getStringParameter(AdminDataKey.town);
		if(town == null){
			town = "";
		}
		areas = getStringParameter(AdminDataKey.areas);
		if(areas == null){
			areas = "[]";
		}
		
		remark = getStringParameter(AdminDataKey.remark);
		if(remark == null){
			remark = "";
		}
		
		return null;
	}
	
	@Override
	protected String execute() {
		BranchInfoModel staffModel = new BranchInfoModel();
		if(!staffModel.create(upbranchid, name, province, city, county, town, areas, remark)){
			return this.errorResult();
		}
		return this.successResult();
	}
}
