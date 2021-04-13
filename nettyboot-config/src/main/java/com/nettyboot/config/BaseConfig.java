package com.nettyboot.config;


public interface BaseConfig {

	interface CommonStatus {
		int editing = 0;
		int online  = 1;
		int offline = 2;
		int deleted = 3;
	}

	interface YesNo {
		int no = 0;
		int yes  = 1;
	}

	enum PostBodyType {
		JSON, FORM
	}

}