package model.xhs;


import com.nettyboot.mysql.XModel;

public class SchoolDataDatabase extends XModel {

	@Override
	protected String getDataSourceName() {
		return "school_data";
	}

}
