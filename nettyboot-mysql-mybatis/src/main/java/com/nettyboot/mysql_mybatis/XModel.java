package com.nettyboot.mysql_mybatis;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @copyright (c) xhigher 2015
 * @author xhigher    2015-3-26
 */
public abstract class XModel {

    protected static Logger logger = LoggerFactory.getLogger(XModel.class);

    private XContext mContext = null;

    protected abstract String getDataSourceName();

    private boolean isBadTransaction() {
        return (mContext == null || mContext.getTransaction() == null || mContext.getTransaction().isEnded());
    }

    public boolean setTransaction(XContext context) {
        mContext = context;
        if (mContext != null && mContext.getTransaction() != null) {
            if (mContext.getTransaction().getSqlSession() == null) {
                return mContext.getTransaction().setConnection(XMySQL.getSqlSession(getDataSourceName(), false));
            }
            return true;
        }
        return false;
    }

    public SqlSession getSqlSession() {
        if (!isBadTransaction()) {
            return mContext.getTransaction().getSqlSession();
        }
        return XMySQL.getSqlSession(getDataSourceName());
    }

    public void closeSqlSession(SqlSession sqlSession) {

        if (isBadTransaction()) {
            XMySQL.releaseSqlSession(sqlSession);
        }
    }



}

