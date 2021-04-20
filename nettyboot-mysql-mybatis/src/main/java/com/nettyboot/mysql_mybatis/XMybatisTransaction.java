package com.nettyboot.mysql_mybatis;

import com.nettyboot.mysql.XBaseTransaction;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public class XMybatisTransaction extends XBaseTransaction<SqlSession> {

	private static final Logger logger = LoggerFactory.getLogger(XMybatisModel.class);
	
	@Override
	public boolean setConnection(SqlSession session) {
		if(connection == null) {
			// 默认获取的都是不自动提交的SqlSession
			connection = session;
		}
		return false;
	}

	@Override
	public boolean end(boolean success){
		if(!isEnded) {
			try {
				if(success) {
					connection.commit(true);
				}else {
					connection.rollback(true);
				}
				return true;
			} catch (Exception e) {
				logger.error("XModelTransaction.end", e);
			} finally {
				XMybatisMySQL.releaseSqlSession(connection);
				isEnded = true;
				connection = null;
			}
		}
		return false;
	}

}

