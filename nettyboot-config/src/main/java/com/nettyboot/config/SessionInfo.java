package com.nettyboot.config;


public abstract class SessionInfo {

	protected String peerid = null;
	protected String sessionid = null;

	protected String userid = null;
	protected String username = null;
	protected Integer type = null;

	public SessionInfo(String peerid, String sessionid) {
		this.peerid = peerid;
		this.sessionid = sessionid;
	}

	public String getPeerid(){
		return this.peerid;
	}

	public String getSessionid() {
		return sessionid;
	}

	public String getUserid() {
		return userid;
	}

	public String getUsername() {
		return username;
	}

	public Integer getType() {
		return type;
	}

	public abstract boolean check();

}
