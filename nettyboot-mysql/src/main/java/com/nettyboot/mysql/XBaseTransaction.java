package com.nettyboot.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public abstract class XBaseTransaction<Conn> {

	private static final Logger logger = LoggerFactory.getLogger(XModel.class);
	
	protected Conn connection;
	
	protected boolean isEnded = false;
	
	public abstract boolean setConnection(Conn conn);

	public abstract boolean end(boolean success);
	
	public Conn getConnection() {
		return connection;
	}

	public boolean isEnded() {
		return this.isEnded;
	}
}

