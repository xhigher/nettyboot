package com.nettyboot.mysql_mybatis.xhs;


import com.nettyboot.mysql_mybatis.beans.StudentInfo;
import com.nettyboot.mysql_mybatis.entrys.StudentInfoMapper;
import org.apache.ibatis.session.SqlSession;

public class StudentInfoModel extends SchoolDataDatabase {

	private SqlSession sqlSession = null;

	public SqlSession getSession(){
		return sqlSession;
	}

	public StudentInfoMapper getMapper(){
		if(sqlSession == null) {
			sqlSession = this.getSqlSession();
		}
		return sqlSession.getMapper(StudentInfoMapper.class);
	}

	public StudentInfo selectById(int id){
		return getMapper().selectById123(id);
	}

	public boolean updateById(int id, String memo){
		return getMapper().updateById(id, memo);
	}



}
