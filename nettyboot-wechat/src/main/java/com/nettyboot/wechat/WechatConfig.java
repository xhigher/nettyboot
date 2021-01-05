package com.nettyboot.wechat;

import java.util.Properties;

public class WechatConfig {

	public enum TradeType {
		APP, // APP支付
		MWEB,// H5支付
		NATIVE,// 扫一扫支付
	}

	private static final String PROP_KEY_NATIVE_APP = "wechat.native_app";
	private static final String PROP_KEY_WEB_APP = "wechat.web_app";
	private static final String PROP_KEY_MP_APP = "wechat.mp_app";
	private static final String PROP_KEY_MINI_APP = "wechat.mini_app";

	private static final String PROP_KEY_SUFFIX_APP_ID = ".app_id";
	private static final String PROP_KEY_SUFFIX_APP_SECRET = ".app_secret";

	private static final String PROP_KEY_SUFFIX_PAY_MCH_ID = ".pay.mch_id";
	private static final String PROP_KEY_SUFFIX_PAY_MCH_SECRET = ".pay.mch_secret";
	private static final String PROP_KEY_SUFFIX_PAY_NOTIFY_URL = ".pay.notify_url";
	private static final String PROP_KEY_SUFFIX_PAY_ORDER_BODY = ".pay.order_body";
	private static final String PROP_KEY_SUFFIX_PAY_CERT_PATH = ".pay.cert_path";

	protected static AppInfo nativeAppInfo = null;
	protected static AppInfo webAppInfo = null;
	protected static AppInfo mpAppInfo = null;
	protected static AppInfo miniAppInfo = null;

	protected static MchInfo nativeMchInfo = null;
	protected static MchInfo webMchInfo = null;

	public static void init(Properties properties){
		nativeAppInfo = initAppInfo(properties, PROP_KEY_NATIVE_APP);
		webAppInfo = initAppInfo(properties, PROP_KEY_WEB_APP);
		mpAppInfo = initAppInfo(properties, PROP_KEY_MP_APP);
		miniAppInfo = initAppInfo(properties, PROP_KEY_MINI_APP);

		nativeMchInfo = initMchInfo(properties, PROP_KEY_NATIVE_APP);
		webMchInfo = initMchInfo(properties, PROP_KEY_WEB_APP);

		if(nativeMchInfo != null || webMchInfo != null){
			WechatPayHelper.init();
		}
	}

	public static AppInfo getNativeAppInfo(){
		return nativeAppInfo;
	}

	public static AppInfo getWebAppInfo(){
		return webAppInfo;
	}

	public static AppInfo getMpAppInfo(){
		return mpAppInfo;
	}

	public static AppInfo getNiniAppInfo(){
		return miniAppInfo;
	}

	public static MchInfo getNativeMchInfo(){
		return nativeMchInfo;
	}

	public static MchInfo getWebMchInfo(){
		return webMchInfo;
	}

	private static AppInfo initAppInfo(Properties properties, String propType){
		AppInfo appInfo = null;
		String idKey = propType + PROP_KEY_SUFFIX_APP_ID;
		String secretKey = propType + PROP_KEY_SUFFIX_APP_SECRET;
		if(properties.containsKey(idKey) && properties.containsKey(secretKey)){
			appInfo = new AppInfo(properties.getProperty(idKey,""),
					properties.getProperty(secretKey,""));
		}
		return appInfo;
	}

	private static MchInfo initMchInfo(Properties properties, String propType){
		MchInfo mchInfo = null;
		String idKey = propType + PROP_KEY_SUFFIX_PAY_MCH_ID;
		String secretKey = propType + PROP_KEY_SUFFIX_PAY_MCH_SECRET;
		String notifyKey = propType + PROP_KEY_SUFFIX_PAY_NOTIFY_URL;
		String bodyKey = propType + PROP_KEY_SUFFIX_PAY_ORDER_BODY;
		String certKey = propType + PROP_KEY_SUFFIX_PAY_CERT_PATH;

		if(properties.containsKey(idKey) && properties.containsKey(secretKey) &&
				properties.containsKey(notifyKey) && properties.containsKey(bodyKey) &&
				properties.containsKey(certKey)){
			mchInfo = new MchInfo(properties.getProperty(idKey,""),
					properties.getProperty(secretKey,""),
					properties.getProperty(notifyKey,""),
					properties.getProperty(bodyKey,""),
					properties.getProperty(certKey,""));
		}
		return mchInfo;
	}

}
