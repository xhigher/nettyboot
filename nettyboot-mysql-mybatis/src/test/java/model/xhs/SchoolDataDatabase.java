package model.xhs;


import com.nettyboot.mysql.XModel;
import entrys.StudentInfoMapper;

public class SchoolDataDatabase extends XModel {

	@Override
	protected String getDataSourceName() {
		return "school_data";
	}

	public StudentInfoMapper getStudentInfoMapper(){
		return this.getSqlSession().getMapper(StudentInfoMapper.class);
	}
}
