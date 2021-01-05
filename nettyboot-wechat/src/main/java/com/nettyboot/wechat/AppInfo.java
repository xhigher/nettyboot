package com.nettyboot.wechat;

public class AppInfo {

	public final String ID;
	public final String SECRET;

	public AppInfo(String appid, String appSecret){
		this.ID = appid;
		this.SECRET = appSecret;
	}

}
