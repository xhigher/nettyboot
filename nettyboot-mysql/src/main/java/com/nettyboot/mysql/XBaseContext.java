package com.nettyboot.mysql;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public abstract class XBaseContext<T extends XBaseTransaction> {

	protected T transaction = null;

	public abstract void startTransaction();
	
	public T getTransaction(){
		return transaction;
	}

	public boolean endTransaction(boolean success){
		boolean flag = false;
		if(transaction != null && !transaction.isEnded()) {
			flag = transaction.end(success);
			transaction = null;
		}
		return flag;
	}
	
	public boolean submitTransaction(){
		boolean flag = false;
		if(transaction != null && !transaction.isEnded()) {
			flag = transaction.end(true);
			transaction = null;
		}
		return flag;
	}
	

}
