package com.nettyboot.wechat;

public class MchInfo {

	public final String ID;
	public final String SECRET;

	public String CERT_FILEPATH;
	public String KEY_STORE_PASSWORD;

	public String NOTIFY_URL;
	public String ORDER_BODY;

	public MchInfo(String mchid, String mchSecret, String certPath, String notifyUrl, String unifiedorderBody){
		this.ID = mchid;
		this.SECRET = mchSecret;
		this.CERT_FILEPATH = certPath;
		this.KEY_STORE_PASSWORD = mchid;
		this.NOTIFY_URL = notifyUrl;
		this.ORDER_BODY = unifiedorderBody;
	}

}
