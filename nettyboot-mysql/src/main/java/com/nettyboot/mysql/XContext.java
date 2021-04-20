package com.nettyboot.mysql;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public final class XContext extends XBaseContext<XTransaction> {

	@Override
	public void startTransaction(){
		transaction = new XTransaction();
	}
	
	@Override
	public XTransaction getTransaction(){
		return transaction;
	}

}
