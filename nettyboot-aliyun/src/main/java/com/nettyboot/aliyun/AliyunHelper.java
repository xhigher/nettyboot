package com.nettyboot.aliyun;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.util.StringUtil;
import com.nettyboot.util.TimeUtil;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class AliyunHelper {

	private static Logger logger = LoggerFactory.getLogger(AliyunHelper.class);
	private static OkHttpClient httpClient = new OkHttpClient();
	
	public static String AppKey = " ";
	public static String AppCode = "";
	
	public static String AccessKey = "";
	public static String AccessSecret = "";

	private static String SMSBaseUrl = "http://dysmsapi.aliyuncs.com/";
	private static String SMSSignName = "";

	public static String SMSTplMsgcodeLogin = "";

	public static void init(Properties properties){
		try {
			AppKey = properties.getProperty("aliyun.app_key", "").trim();
			if (AppKey.isEmpty()) {
				logger.error("init: lacked property[aliyun.app_key]");
			}
			AppCode = properties.getProperty("aliyun.app_code", "").trim();
			if (AppCode.isEmpty()) {
				logger.error("init: lacked property[aliyun.app_code]");
			}
			AccessKey = properties.getProperty("aliyun.access_key", "").trim();
			if (AccessKey.isEmpty()) {
				logger.error("init: lacked property[aliyun.access_key]");
			}
			AccessSecret = properties.getProperty("aliyun.access_secret", "").trim();
			if (AccessSecret.isEmpty()) {
				logger.error("init: lacked property[aliyun.access_secret]");
			}
			SMSSignName = properties.getProperty("aliyun.sms_sign_name", "").trim();
			if (SMSSignName.isEmpty()) {
				logger.error("init: lacked property[aliyun.sms_sign_name]");
			}
			SMSTplMsgcodeLogin = properties.getProperty("aliyun.sms_tpl_msgcode_login", "").trim();
			if (SMSTplMsgcodeLogin.isEmpty()) {
				logger.error("init: lacked property[aliyun.sms_tpl_msgcode_login]");
			}
		}catch (Exception e){
			logger.error("init", e);
		}
	}

	public static boolean sendLoginMsgcode(final String tel, final String msgcode) {
		return sendMsgcode(SMSTplMsgcodeLogin, tel, msgcode);
	}

	// public static boolean sendPasswordMsgcode(final String tel, final String
	// msgcode){
	// return sendMsgcode(SMSTplMsgcodePassword, tel, msgcode);
	// }

	public static boolean sendMsgcode(String templateCode, final String tel, final String msgcode) {
		try {
			String smsUrl = getSMSUrl(templateCode, tel, msgcode);
			System.out.println("ALiYunSMSHelper.sendMsgcode.smsUrl=" + smsUrl);
			HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(smsUrl).newBuilder();

			Request request = new Request.Builder().url(httpUrlBuilder.build()).build();
			Response response = httpClient.newCall(request).execute();
			String responseString = response.body().string();
			if (responseString != null) {
				logger.info("ALiYunSMSHelper.sendMsgcode:tel=" + tel + ", responseString=" + responseString);
				JSONObject responseJSON = JSONObject.parseObject(responseString);
				if (responseJSON != null && responseJSON.containsKey("Code")
						&& "OK".equals(responseJSON.getString("Code").toUpperCase())) {
					return true;
				} else {
					logger.error(
							"ALiYunSMSHelper.sendMsgcode.Failed:tel=" + tel + ", responseString=" + responseString);
				}
			} else {
				logger.error("ALiYunSMSHelper.sendMsgcode.Failed:tel=" + tel);
			}
		} catch (Exception e) {
			logger.error("ALiYunSMSHelper.sendMsgcode.Exception", e);
		}
		return false;
	}

	private static String getSMSUrl(String templateCode, String tel, String msgcode) throws Exception {
		Map<String, String> params = new TreeMap<String, String>();
		params.put("SignatureMethod", "HMAC-SHA1");
		params.put("SignatureNonce", StringUtil.randomString(16, false));
		params.put("AccessKeyId", AccessKey);
		params.put("SignatureVersion", "1.0");
		params.put("Timestamp", TimeUtil.getYMDTHMSZ());
		params.put("Format", "JSON");

		JSONObject templateParam = new JSONObject();
		templateParam.put("code", msgcode);
		params.put("Action", "SendSms");
		params.put("Version", "2017-05-25");
		params.put("RegionId", "cn-hangzhou");
		params.put("PhoneNumbers", tel);
		params.put("SignName", SMSSignName);
		params.put("TemplateCode", templateCode);
		params.put("TemplateParam", templateParam.toJSONString());
		params.put("OutId", "" + System.currentTimeMillis());

		java.util.Iterator<String> it = params.keySet().iterator();
		StringBuilder sortQueryStringTmp = new StringBuilder();
		while (it.hasNext()) {
			String key = it.next();
			sortQueryStringTmp.append("&").append(StringUtil.specialUrlEncode(key)).append("=")
					.append(StringUtil.specialUrlEncode(params.get(key)));
		}
		String sortedQueryString = sortQueryStringTmp.substring(1);

		StringBuilder stringToSign = new StringBuilder();
		stringToSign.append("GET").append("&");
		stringToSign.append(StringUtil.specialUrlEncode("/")).append("&");
		stringToSign.append(StringUtil.specialUrlEncode(sortedQueryString));

		String sign = StringUtil.getHmacSHA1Encrypt(stringToSign.toString(), AccessSecret + "&");

		String signature = StringUtil.specialUrlEncode(sign);

		return SMSBaseUrl + "?Signature=" + signature + sortQueryStringTmp;
	}
}
