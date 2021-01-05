package com.nettyboot.wechat;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.http.HttpClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class WechatHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(WechatHelper.class);

	private static final String BASE_URL = "https://api.weixin.qq.com";

	private static final String URL_GET_USER_INFO = BASE_URL + "/sns/userinfo";
	private static final String URL_OAUTH2_ACCESS_TOKEN = BASE_URL + "/sns/oauth2/access_token";

	private static final String URL_CODE2SESSION = BASE_URL + "/sns/jscode2session";

	private static final String URL_ACCESS_TOKEN = BASE_URL + "/cgi-bin/token";
	public static final String URL_GET_TICKET = BASE_URL + "/cgi-bin/ticket/getticket";

	private static final String URL_MENU_CREATE = BASE_URL + "/cgi-bin/menu/create";

	private static final String URL_BATCHGET_MATERIAL =  BASE_URL + "/cgi-bin/material/batchget_material";
	private static final String URL_GET_MATERIAL = BASE_URL + "/cgi-bin/material/get_material";

	public static JSONObject code2session(String code){
		if(WechatConfig.miniAppInfo == null){
			logger.error("code2session.failed:miniAppInfo=null");
			return null;
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("appid", WechatConfig.miniAppInfo.ID);
		params.put("secret", WechatConfig.miniAppInfo.SECRET);
		params.put("grant_type", "authorization_code");
		params.put("js_code", code);
		try {
			String resultString = HttpClientHelper.post(URL_CODE2SESSION, params);
			JSONObject resultData = JSONObject.parseObject(resultString);
			if(resultData != null && resultData.containsKey("session_key")){
				return resultData;
			}
			logger.error("code2session.failed:resultString="+resultString);
		} catch (IOException e) {
			logger.error("code2session.Exception", e);
		}
		return null;
	}

	public static JSONObject decryptUserData(String sessionKey, String encryptedData,  String iv) {
		try {
			byte[] keyByte = Base64.decodeBase64(sessionKey);
			byte[] dataByte = Base64.decodeBase64(encryptedData);
			byte[] ivByte = Base64.decodeBase64(iv);

			int base = 16;
			if (keyByte.length % base != 0) {
				int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
				byte[] temp = new byte[groups * base];
				Arrays.fill(temp, (byte) 0);
				System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
				keyByte = temp;
			}
			Security.addProvider(new BouncyCastleProvider());

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
			Key spec = new SecretKeySpec(keyByte, "AES");
			AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
			parameters.init(new IvParameterSpec(ivByte));
			cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
			byte[] resultByte = cipher.doFinal(dataByte);
			System.out.println("resultByte="+resultByte);
			if (null != resultByte && resultByte.length > 0) {
				String result = new String(resultByte, "UTF-8");
				return JSONObject.parseObject(result);
			}
		}catch (Exception e) {
			logger.error("WechatHelper.getUserInfo.Exception", e);
		}
		return null;
	}

	public static JSONObject getOauth2AccessToken(AppInfo appInfo, String code){
		Map<String, String> params = new HashMap<String, String>();
		params.put("appid", appInfo.ID);
		params.put("secret", appInfo.SECRET);
		params.put("grant_type", "authorization_code");
		params.put("code", code);
		try {
			String resultString = HttpClientHelper.post(URL_OAUTH2_ACCESS_TOKEN, params);
			JSONObject resultData = JSONObject.parseObject(resultString);
			logger.info("getOauth2AccessToken:resultString"+resultString);
			if(resultData != null && resultData.getString("access_token") != null){
				return resultData;
			}
			logger.error("getOauth2AccessToken.failed:resultString"+resultString);
		} catch (IOException e) {
			logger.error("getOauth2AccessToken.Exception", e);
		}
		return null;
	}
	
	public static JSONObject getUserInfo(String userAccessToken, String openid){
		Map<String, String> params = new HashMap<String, String>();
		params.put("openid", openid);
		params.put("access_token", userAccessToken);
		params.put("lang", "zh_CN");
		try {
			String resultString = HttpClientHelper.get(URL_GET_USER_INFO, params);
			JSONObject resultData = JSONObject.parseObject(resultString);
			logger.info("getUserInfo:resultString"+resultString);
			if(resultData != null && resultData.containsKey("openid")){
				return resultData;
			}
			logger.error("getUserInfo.failed:resultString"+resultString);
		} catch (IOException e) {
			logger.error("getUserInfo.Exception", e);
		}
		return null;
	}

	public static JSONObject getAccessToken(AppInfo appInfo){
		Map<String, String> params = new HashMap<String, String>();
		params.put("appid", appInfo.ID);
		params.put("secret", appInfo.SECRET);
		params.put("grant_type", "client_credential");
		try {
			String resultString = HttpClientHelper.get(URL_ACCESS_TOKEN, params);
			JSONObject resultData = JSONObject.parseObject(resultString);
			if(resultData != null && resultData.containsKey("access_token")){
				return resultData;
			}
		} catch (IOException e) {
			logger.error("getAccessToken.Exception", e);
		}
		return null;
	}

	public static JSONObject getJSApiTicket(String accessToken){
		Map<String, String> params = new HashMap<String, String>();
		params.put("type", "jsapi");
		params.put("access_token", accessToken);
		try {
			String resultString = HttpClientHelper.get(URL_GET_TICKET, params);
			JSONObject resultData = JSONObject.parseObject(resultString);
			if(resultData != null && resultData.containsKey("ticket")){
				return resultData;
			}
		} catch (IOException e) {
			logger.error("getJSApiTicket.Exception", e);
		}
		return null;
	}

	public static boolean updateMenu(String accessToken, final JSONObject configData){
		try {
			String responseString = HttpClientHelper.post(URL_MENU_CREATE+"?access_token="+accessToken, configData);
			JSONObject resultData = JSONObject.parseObject(responseString);
			if(resultData != null && resultData.containsKey("errcode") && resultData.getIntValue("errcode")==0){
				return true;
			}
			logger.info("updateMenu.failed:responseString="+responseString);
		} catch (IOException e) {
			logger.error("updateMenu.Exception", e);
		}
		return false;
	}

	public static JSONObject batchGetMaterial(String accessToken) {
		JSONObject paramsJSON = new JSONObject();
		paramsJSON.put("type", "news");
		paramsJSON.put("offset", 0);
		paramsJSON.put("count", 20);
		try {
			String responseString = HttpClientHelper.post(URL_BATCHGET_MATERIAL+"?access_token="+accessToken, paramsJSON);
			logger.info("batchGetMaterial:responseString="+responseString);
			if(responseString!=null && !responseString.isEmpty()){
				JSONObject resultData = JSONObject.parseObject(responseString);
				return resultData;
			}
			logger.info("batchGetMaterial.failed:responseString="+responseString);
		} catch (IOException e) {
			logger.error("batchGetMaterial.Exception", e);
		}
		return null;
	}

	public static JSONObject getMaterial(String accessToken, String mediaid){
		JSONObject paramsJSON = new JSONObject();
		paramsJSON.put("media_id", mediaid);
		try {
			String responseString = HttpClientHelper.post(URL_GET_MATERIAL+"?access_token="+accessToken, paramsJSON);
			logger.info("getMaterial:responseString="+responseString);
			if(responseString!=null && !responseString.isEmpty()){
				JSONObject resultData = JSONObject.parseObject(responseString);
				if(resultData!=null && resultData.containsKey("news_item")){
					resultData = resultData.getJSONArray("news_item").getJSONObject(0);
					resultData.remove("content");
				}
				return resultData;
			}
			logger.info("getMaterial.failed:responseString="+responseString);
		} catch (IOException e) {
			logger.error("getMaterial.Exception", e);
		}
		return null;
	}
	
		
}
