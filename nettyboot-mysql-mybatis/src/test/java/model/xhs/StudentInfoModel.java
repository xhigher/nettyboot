package model.xhs;


import beans.StudentInfo;
import com.nettyboot.mysql.XModel;
import entrys.StudentInfoMapper;
import org.apache.ibatis.session.SqlSession;

public class StudentInfoModel extends SchoolDataDatabase {

	private SqlSession sqlSession;

	public StudentInfoMapper getMapper(){
		sqlSession = this.getSqlSession();
		return sqlSession.getMapper(StudentInfoMapper.class);
	}

	public StudentInfo selectById(int id){
		try {
			return getMapper().selectById(id);
		}finally {
			this.closeSqlSession(sqlSession);
		}
	}

	public boolean updateById(int id, String memo){
		try {
			return getMapper().updateById(id, memo);
		}finally {
			this.closeSqlSession(sqlSession);
		}
	}

}
