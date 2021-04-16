package com.nettyboot.mysql;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public class XTransaction {

	private static final Logger logger = LoggerFactory.getLogger(XModel.class);
	
	private SqlSession sqlSession;
	
	private boolean isEnded = false;
	
	public boolean setConnection(SqlSession conn) {
		if(sqlSession == null) {
			sqlSession = conn;
			try {
				sqlSession.getConnection().setAutoCommit(false);
				return true;
			} catch (SQLException e) {
				logger.error("XModelTransaction.begin", e);
			}
		}
		return false;
	}
	
	public SqlSession getSqlSession() {
		return sqlSession;
	}
	
	public boolean isEnded() {
		return this.isEnded;
	}
	
	public boolean end(boolean success){
		if(!isEnded) {
			try {
				if(success) {
					sqlSession.commit();
				}else {
					sqlSession.rollback();
				}
				sqlSession.getConnection().setAutoCommit(true);
				return true;
			} catch (SQLException e) {
				logger.error("XModelTransaction.end", e);
			} finally {
				XMySQL.releaseSqlSession(sqlSession);
				isEnded = true;
				sqlSession = null;
			}
		}
		return false;
	}

}

