package com.nettyboot.config;


public enum ClientType {

	unknown(10),
	app_ios(11),
	app_android(12),
	web_pc(13),
	web_mobile(14),
	wechat_mp(15),
	wechat_miniapp(16),
	;

	private final int id;

	ClientType(int id){
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static ClientType of(int id){
		ClientType[] values = ClientType.values();
		for(int i=0; i<values.length; i++) {
			if(values[i].id == id) {
				return values[i];
			}
		}
		return ClientType.unknown;
	}


}