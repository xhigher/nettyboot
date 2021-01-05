package com.nettyboot.wechat;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.util.StringUtil;
import okhttp3.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WechatPayHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(WechatPayHelper.class);
	
	private static OkHttpClient nativeHttpClient = null;
	private static OkHttpClient webHttpClient = null;

	private static final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml; charset=utf-8");

	private static final String URL_PAY_UNIFIEDORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	public static void init(){
		if(WechatConfig.nativeMchInfo != null){
			nativeHttpClient = initHttpClient(WechatConfig.nativeMchInfo);
		}
		if(WechatConfig.webMchInfo != null){
			webHttpClient = initHttpClient(WechatConfig.webMchInfo);
		}
	}

	public static boolean signCheck(JSONObject params){
		if(WechatConfig.nativeMchInfo != null){
			return signCheck(WechatConfig.nativeMchInfo, params);
		}else if(WechatConfig.webMchInfo != null){
			return signCheck(WechatConfig.webMchInfo, params);
		}
		return false;
	}

	public static boolean signCheckNative(JSONObject params){
		return signCheck(WechatConfig.nativeMchInfo, params);
	}

	public static boolean signCheckWeb(JSONObject params){
		return signCheck(WechatConfig.webMchInfo, params);
	}

	public static JSONObject unifiedOrderNative(String paymentid, long money, String userid, WechatConfig.TradeType tradeType, String clientIP){
		return unifiedOrder(nativeHttpClient, WechatConfig.nativeMchInfo, WechatConfig.nativeAppInfo, paymentid, money, userid, tradeType, clientIP);
	}

	public static JSONObject unifiedOrderWeb(AppInfo appInfo, String paymentid, long money, String userid, WechatConfig.TradeType tradeType, String clientIP){
		return unifiedOrder(webHttpClient, WechatConfig.webMchInfo, appInfo, paymentid, money, userid, tradeType, clientIP);
	}

	public static JSONObject unifiedOrderWeb(String paymentid, long money, String userid, WechatConfig.TradeType tradeType, String clientIP){
		AppInfo appInfo = null;
		if(WechatConfig.mpAppInfo != null){
			appInfo = WechatConfig.mpAppInfo;
		}else if(WechatConfig.miniAppInfo != null){
			appInfo = WechatConfig.miniAppInfo;
		}else if(WechatConfig.webAppInfo != null){
			appInfo = WechatConfig.webAppInfo;
		}
		if(appInfo != null){
			return unifiedOrderWeb(appInfo, paymentid, money, userid, tradeType, clientIP);
		}
		return null;
	}

	public static OkHttpClient initHttpClient(MchInfo mchInfo){
		InputStream fis = null;
		try{
			KeyStore keyStore  = KeyStore.getInstance("PKCS12");
			fis = new FileInputStream(new File(mchInfo.CERT_FILEPATH));
	        keyStore.load(fis, mchInfo.KEY_STORE_PASSWORD.toCharArray());

	        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        trustManagerFactory.init((KeyStore) null);
	        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
	        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
	        	logger.error("init.error:Unexpected default trust managers");
	        }
	        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
	        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	        keyManagerFactory.init(keyStore, mchInfo.KEY_STORE_PASSWORD.toCharArray());
	        
	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { trustManager }, null);
	        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

	        return new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, trustManager).build();

		}catch(Exception e){
			logger.error("init", e);
		}finally{
			try {
				fis.close();
			} catch (IOException e) { 
                logger.error("init", e);
           } 
		}
		return null;
	}

	public static boolean signCheck(MchInfo mchInfo, JSONObject params){
		String sign = params.getString("sign");
		params.remove("sign");
		StringBuffer content = new StringBuffer();
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.getString(key);
			content.append((i == 0 ? "" : "&") + key + "=" + value);
		}
		content.append("&").append("key=").append(mchInfo.SECRET);
		String tempSign = StringUtil.md5(content.toString()).toUpperCase();
		return tempSign.equals(sign);
	}
	
	public static JSONObject unifiedOrder(OkHttpClient client, MchInfo mchInfo, AppInfo appInfo, String paymentid, long money, String userid, WechatConfig.TradeType tradeType, String clientIP){
		try {
			String nonceStr = StringUtil.randomString(20, false);
			StringBuilder signValueSB = new StringBuilder();
			signValueSB.append("appid=").append(appInfo.ID).append("&");
			signValueSB.append("attach=").append(userid).append("&");
			signValueSB.append("body=").append(mchInfo.ORDER_BODY).append("&");
			signValueSB.append("device_info=WEB&");
			signValueSB.append("fee_type=CNY&");
			signValueSB.append("mch_id=").append(mchInfo.ID).append("&");
			signValueSB.append("nonce_str=").append(nonceStr).append("&");
			signValueSB.append("notify_url=").append(mchInfo.NOTIFY_URL).append("&");
			//signValueSB.append("openid=").append(openid).append("&");
			signValueSB.append("out_trade_no=").append(paymentid).append("&");
//			signValueSB.append("scene_info=").append(SCENE_INFO).append("&");
			signValueSB.append("sign_type=MD5&");
			signValueSB.append("spbill_create_ip=").append(clientIP).append("&");
			signValueSB.append("total_fee=").append(money).append("&");
			signValueSB.append("trade_type=").append(tradeType.name()).append("&");
			signValueSB.append("key=").append(mchInfo.SECRET);
			String sign = StringUtil.md5(signValueSB.toString()).toUpperCase();

			StringBuilder dataXML = new StringBuilder();
			dataXML.append("<xml>");
			dataXML.append("<appid>").append(appInfo.ID).append("</appid>");
			dataXML.append("<attach>").append(userid).append("</attach>");
			dataXML.append("<body>").append(mchInfo.ORDER_BODY).append("</body>");
			dataXML.append("<device_info>WEB</device_info>");
			dataXML.append("<fee_type>CNY</fee_type>");
			dataXML.append("<mch_id>").append(mchInfo.ID).append("</mch_id>");
			dataXML.append("<nonce_str>").append(nonceStr).append("</nonce_str>");
			dataXML.append("<notify_url>").append(mchInfo.NOTIFY_URL).append("</notify_url>");
			//dataXML.append("<openid>").append(openid).append("</openid>");
			dataXML.append("<out_trade_no>").append(paymentid).append("</out_trade_no>");
//			dataXML.append("<scene_info>").append(SCENE_INFO).append("</scene_info>");
			dataXML.append("<sign_type>MD5</sign_type>");
			dataXML.append("<spbill_create_ip>").append(clientIP).append("</spbill_create_ip>");
			dataXML.append("<total_fee>").append(money).append("</total_fee>");
			dataXML.append("<trade_type>").append(tradeType.name()).append("</trade_type>");
			dataXML.append("<sign>").append(sign).append("</sign>");
			dataXML.append("</xml>");

		    Headers.Builder headersBuider = new Headers.Builder();
	        headersBuider.add("Authorization", "text/xml");
	        headersBuider.add("User-Agent", "WXPAY-"+mchInfo.ID);
	        RequestBody bodyData = RequestBody.create(dataXML.toString(), MEDIA_TYPE_XML);
	        Request request = new Request.Builder().url(URL_PAY_UNIFIEDORDER).headers(headersBuider.build()).post(bodyData).build();
	        Response response = client.newCall(request).execute();
	        String resultString = response.body().string();
			logger.info("unifiedOrder:resultString="+resultString);
			JSONObject resultData = parseResultXML(resultString);
			if(resultData != null){
				if(resultData.containsKey("return_code") && "SUCCESS".equals(resultData.getString("result_code"))){
					resultData.put("result", 1);
				}else{
					resultData = new JSONObject();
					resultData.put("result", 0);
				}
			}
			return resultData;
		} catch (IOException e) {
			logger.error("unifiedOrder.Exception", e);
		}
		return null;
	}

	public static JSONObject parseResultXML(String xmlString){
		try {
			JSONObject resultJSON = new JSONObject();
			Document document = DocumentHelper.parseText(xmlString);
	        List<Element> childList = document.getRootElement().elements();
	        Element child = null;
	        for(int i=0, n=childList.size(); i<n; i++){
	        	child = childList.get(i);
	        	resultJSON.put(child.getName(), child.getText());
	        }
	        return resultJSON;
		} catch (Exception e) {
			logger.error("parseResultXML.Exception", e);
		}
        return null;
	}



}
