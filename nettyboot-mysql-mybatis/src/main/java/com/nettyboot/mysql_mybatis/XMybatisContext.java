package com.nettyboot.mysql_mybatis;

import com.nettyboot.mysql.XBaseContext;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public final class XMybatisContext extends XBaseContext<XMybatisTransaction> {

	@Override
	public void startTransaction(){
		transaction = new XMybatisTransaction();
	}
	
	@Override
	public XMybatisTransaction getTransaction(){
		return transaction;
	}

}
