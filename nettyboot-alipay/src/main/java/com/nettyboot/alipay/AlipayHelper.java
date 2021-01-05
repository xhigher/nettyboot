package com.nettyboot.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.nettyboot.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

public class AlipayHelper {
	private static final Logger logger = LoggerFactory.getLogger(AlipayHelper.class);
	
	//支付宝网关（固定）
	private static final String GATEWAY_URL = "https://openapi.alipay.com/gateway.do";

	//参数返回格式，只支持json
	private static final String FORMAT = "json";
	//请求和签名使用的字符编码格式，支持GBK和UTF-8
	private static final String CHARSET = "UTF-8";
	//商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2
	private static final String SIGN_TYPE = "RSA2";

	//值为authorization_code时，代表用code换取；值为refresh_token时，代表用refresh_token换取
	private static final String AUTHORIZATION_CODE = "authorization_code";
	private static final String REFRESH_TOKEN = "refresh_token";

	private static AlipayClient alipayClient = null;

	// 支付宝账户登录授权业务：入参pid值 
	private static String PID = "";
	//APPID即创建应用后生成
	private static String APP_ID = "";
	//开发者应用私钥，由开发者自己生成
	private static String APP_PRIVATE_KEY = "";
	//支付宝公钥，由支付宝生成
	private static String ALIPAY_PUBLIC_KEY = "";
	private static String PAYMENT_SUBJECT = "";
	
	private static String PAYMENT_NOTIFY_URL = "";
	private static String WEBPAY_URL = "";

	private static boolean propertiesOK = true;

	public static void init(Properties properties){
		try {
			PID = properties.getProperty("alipay.pid", "").trim();
			if(PID.isEmpty()){
				propertiesOK = false;
				logger.error("init: lacked property[alipay.pid]");
			}
			APP_ID = properties.getProperty("alipay.app_id", "").trim();
			if(APP_ID.isEmpty()){
				propertiesOK = false;
				logger.error("init: lacked property[alipay.app_id]");
			}
			APP_PRIVATE_KEY = properties.getProperty("alipay.private_key", "").trim();
			if(APP_PRIVATE_KEY.isEmpty()){
				propertiesOK = false;
				logger.error("init: lacked property[alipay.private_key]");
			}
			ALIPAY_PUBLIC_KEY = properties.getProperty("alipay.public_key", "").trim();
			if(ALIPAY_PUBLIC_KEY.isEmpty()){
				propertiesOK = false;
				logger.error("init: lacked property[alipay.public_key]");
			}
			PAYMENT_SUBJECT = properties.getProperty("alipay.payment_subject", "").trim();
			if(PAYMENT_SUBJECT.isEmpty()){
				propertiesOK = false;
				logger.error("init: lacked property[alipay.payment_subject]");
			}
			PAYMENT_NOTIFY_URL = properties.getProperty("alipay.payment_notify_url", "").trim();
			if(PAYMENT_NOTIFY_URL.isEmpty()){
				propertiesOK = false;
				logger.error("init: lacked property[alipay.payment_notify_url]");
			}
			WEBPAY_URL = properties.getProperty("alipay.webpay_url", "").trim();
			if(WEBPAY_URL.isEmpty()){
				propertiesOK = false;
				logger.error("init: lacked property[alipay.webpay_url]");
			}

			if(propertiesOK){
				alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
			}
		}catch (Exception e){
			logger.error("init", e);
		}
	}

	public static boolean signCheck(JSONObject data, String sign_type) {
		try {
			Map<String, String> alipayParams = new HashMap<String, String>();
			for (String key : data.keySet()) {
				alipayParams.put(key, data.getString(key));
			}
			System.out.println("alipayParams = " + alipayParams);
			if (AlipaySignature.rsaCheckV1(alipayParams, ALIPAY_PUBLIC_KEY, CHARSET, sign_type)) {
				return true;
			}
		} catch (AlipayApiException e) {
			logger.error("signCheck", e);
		}
		return false;
	}
	
	public static JSONObject getAccessToken(String authCode){
		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
		request.setGrantType(AUTHORIZATION_CODE);
		request.setCode(authCode);
		
		AlipaySystemOauthTokenResponse response;
		try {
			response = alipayClient.execute(request);
			if(response.isSuccess()){
				return JSONObject.parseObject(response.getBody()).getJSONObject("alipay_system_oauth_token_response");
			} else {
				logger.error("getAccessToken:data="+response.getBody()+",authCode="+authCode);
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			logger.error("getAccessToken:AlipayApiException",e);
		}
		return null;
	}
	
	public static JSONObject getRefreshAccessToken(String refreshToken){
		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
		request.setGrantType(REFRESH_TOKEN);
		request.setRefreshToken(refreshToken);
		
		AlipaySystemOauthTokenResponse response;
		try {
			response = alipayClient.execute(request);
			if(response.isSuccess()){
				return JSONObject.parseObject(response.getBody()).getJSONObject("alipay_system_oauth_token_response");
			} else {
				logger.error("getAccessToken:data="+response.getBody()+",refreshToken="+refreshToken);
			}
		} catch (AlipayApiException e) {
			logger.error("getAccessToken:AlipayApiException",e);
		}
		
		return null;
	}
	
	public static JSONObject getUserInfo(String accessToken){
		AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
		AlipayUserInfoShareResponse response;
		try {
			response = alipayClient.execute(request,accessToken);
			if(response.isSuccess()){
				return JSONObject.parseObject(response.getBody()).getJSONObject("alipay_user_info_share_response");
			} else {
				logger.error("getUserInfo:data="+response.getBody()+",accessToken="+accessToken);
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String appPay(String paymentid, String userid, String amount, String body){
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		AlipayTradeAppPayResponse response = null;
		try {
			JSONObject bizContent = new JSONObject();
			bizContent.put("timeout_express", "30m");
			bizContent.put("product_code", "QUICK_MSECURITY_PAY");
			bizContent.put("total_amount", amount);
			bizContent.put("subject", PAYMENT_SUBJECT);
			bizContent.put("body", body);
			bizContent.put("out_trade_no", paymentid);
			bizContent.put("passback_params", URLEncoder.encode(userid, CHARSET));
			bizContent.put("goods_type", "1");
			//bizContent.put("enable_pay_channels", "pcreditpayInstallment");
			//bizContent.put("extend_params", "{\"hb_fq_num\":12,\"hb_fq_seller_percent\":100}");
			
			request.setBizContent(bizContent.toJSONString());
			request.setNotifyUrl(PAYMENT_NOTIFY_URL);
			response = alipayClient.sdkExecute(request);
			if(response.isSuccess()){
				String resultStr = response.getBody();
				logger.info(resultStr);
				return resultStr;
			}
		} catch (Exception e) {
			logger.error("appPay.failed:paymentid="+paymentid, e);
		}
		return null;
	}

	public static String wapPay(String paymentid, String userid, String amount, String body, String returnUrl){
		AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
		AlipayTradeWapPayResponse response = null;
		try {
			JSONObject bizContent = new JSONObject();
			bizContent.put("timeout_express", "30m");
			bizContent.put("product_code", "QUICK_MSECURITY_PAY");
			bizContent.put("total_amount", amount);
			bizContent.put("subject", PAYMENT_SUBJECT);
			bizContent.put("body", body);
			bizContent.put("out_trade_no", paymentid);
			bizContent.put("passback_params", URLEncoder.encode(userid, CHARSET));
			bizContent.put("goods_type", "1");
			//bizContent.put("enable_pay_channels", "pcreditpayInstallment");
			//bizContent.put("extend_params", "{\"hb_fq_num\":12,\"hb_fq_seller_percent\":100}");

			request.setBizContent(bizContent.toJSONString());
			request.setNotifyUrl(PAYMENT_NOTIFY_URL);
			if(returnUrl != null){
				request.setReturnUrl(returnUrl);
			}else{
				request.setReturnUrl(WEBPAY_URL);
			}
			response = alipayClient.sdkExecute(request);
			if(response.isSuccess()){
				String resultStr = response.getBody();
				logger.info(resultStr);
				return resultStr;
			}
		} catch (Exception e) {
			logger.error("appPay.failed:paymentid="+paymentid, e);
		}
		return null;
	}
	
	public static Map<String, String> buildAuthInfoMap(boolean rsa2) {
		String target_id = TimeUtil.getCurrentYMDHMSS();
		Map<String, String> keyValues = new HashMap<String, String>();
		keyValues.put("app_id", AlipayHelper.APP_ID);
		keyValues.put("pid", AlipayHelper.PID);
		keyValues.put("apiname", "com.alipay.account.auth");
		keyValues.put("app_name", "mc");
		keyValues.put("biz_type", "openservice");
		keyValues.put("product_id", "APP_FAST_LOGIN");
		keyValues.put("scope", "kuaijie");
		keyValues.put("target_id", target_id);
		keyValues.put("auth_type", "AUTHACCOUNT");
		keyValues.put("sign_type", rsa2 ? "RSA2" : "RSA");
		keyValues.put("method","alipay.open.auth.sdk.code.get");
		
		return keyValues;
	}
	
	public static String buildOrderParam(Map<String, String> map) {
		List<String> keys = new ArrayList<String>(map.keySet());

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.size() - 1; i++) {
			String key = keys.get(i);
			String value = map.get(key);
			sb.append(buildKeyValue(key, value, true));
			sb.append("&");
		}

		String tailKey = keys.get(keys.size() - 1);
		String tailValue = map.get(tailKey);
		sb.append(buildKeyValue(tailKey, tailValue, true));
		
		return sb.toString();
	}
	
	private static String buildKeyValue(String key, String value, boolean isEncode) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append("=");
		if (isEncode) {
			try {
				sb.append(URLEncoder.encode(value, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				sb.append(value);
			}
		} else {
			sb.append(value);
		}
		return sb.toString();
	}
	

	public static String getSign(Map<String, String> map, boolean rsa2) {
		List<String> keys = new ArrayList<String>(map.keySet());

		Collections.sort(keys);

		StringBuilder authInfo = new StringBuilder();
		for (int i = 0; i < keys.size() - 1; i++) {
			String key = keys.get(i);
			String value = map.get(key);
			authInfo.append(buildKeyValue(key, value, false));
			authInfo.append("&");
		}

		String tailKey = keys.get(keys.size() - 1);
		String tailValue = map.get(tailKey);
		authInfo.append(buildKeyValue(tailKey, tailValue, false));

		String oriSign = sign(authInfo.toString(), AlipayHelper.APP_PRIVATE_KEY, rsa2);
		String encodedSign = "";

		try {
			encodedSign = URLEncoder.encode(oriSign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "sign=" + encodedSign;
	}
	
	private static String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}
	
	private static final String ALGORITHM = "RSA";

	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	private static final String SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA";

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static String getAlgorithms(boolean rsa2) {
		return rsa2 ? SIGN_SHA256RSA_ALGORITHMS : SIGN_ALGORITHMS;
	}
	
	public static String sign(String content, String privateKey, boolean rsa2) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					AlipayBase64.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature
					.getInstance(getAlgorithms(rsa2));

			signature.initSign(priKey);
			signature.update(content.getBytes(DEFAULT_CHARSET));

			byte[] signed = signature.sign();

			return AlipayBase64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	static final class AlipayBase64 {

		private static final int BASELENGTH = 128;
		private static final int LOOKUPLENGTH = 64;
		private static final int TWENTYFOURBITGROUP = 24;
		private static final int EIGHTBIT = 8;
		private static final int SIXTEENBIT = 16;
		private static final int FOURBYTE = 4;
		private static final int SIGN = -128;
		private static final char PAD = '=';
		private static final byte[] base64Alphabet = new byte[BASELENGTH];
		private static final char[] lookUpBase64Alphabet = new char[LOOKUPLENGTH];

		static {
			for (int i = 0; i < BASELENGTH; ++i) {
				base64Alphabet[i] = -1;
			}
			for (int i = 'Z'; i >= 'A'; i--) {
				base64Alphabet[i] = (byte) (i - 'A');
			}
			for (int i = 'z'; i >= 'a'; i--) {
				base64Alphabet[i] = (byte) (i - 'a' + 26);
			}

			for (int i = '9'; i >= '0'; i--) {
				base64Alphabet[i] = (byte) (i - '0' + 52);
			}

			base64Alphabet['+'] = 62;
			base64Alphabet['/'] = 63;

			for (int i = 0; i <= 25; i++) {
				lookUpBase64Alphabet[i] = (char) ('A' + i);
			}

			for (int i = 26, j = 0; i <= 51; i++, j++) {
				lookUpBase64Alphabet[i] = (char) ('a' + j);
			}

			for (int i = 52, j = 0; i <= 61; i++, j++) {
				lookUpBase64Alphabet[i] = (char) ('0' + j);
			}
			lookUpBase64Alphabet[62] = '+';
			lookUpBase64Alphabet[63] = '/';

		}

		private static boolean isWhiteSpace(char octect) {
			return (octect == 0x20 || octect == 0xd || octect == 0xa || octect == 0x9);
		}

		private static boolean isPad(char octect) {
			return (octect == PAD);
		}

		private static boolean isData(char octect) {
			return (octect < BASELENGTH && base64Alphabet[octect] != -1);
		}

		/**
		 * Encodes hex octects into Base64
		 * 
		 * @param binaryData
		 *            Array containing binaryData
		 * @return Encoded Base64 array
		 */
		public static String encode(byte[] binaryData) {

			if (binaryData == null) {
				return null;
			}

			int lengthDataBits = binaryData.length * EIGHTBIT;
			if (lengthDataBits == 0) {
				return "";
			}

			int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
			int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
			int numberQuartet = fewerThan24bits != 0 ? numberTriplets + 1
					: numberTriplets;
			char[] encodedData = null;

			encodedData = new char[numberQuartet * 4];

			byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;

			int encodedIndex = 0;
			int dataIndex = 0;

			for (int i = 0; i < numberTriplets; i++) {
				b1 = binaryData[dataIndex++];
				b2 = binaryData[dataIndex++];
				b3 = binaryData[dataIndex++];

				l = (byte) (b2 & 0x0f);
				k = (byte) (b1 & 0x03);

				byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
						: (byte) ((b1) >> 2 ^ 0xc0);
				byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4)
						: (byte) ((b2) >> 4 ^ 0xf0);
				byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6)
						: (byte) ((b3) >> 6 ^ 0xfc);

				encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[(l << 2) | val3];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3f];
			}

			// form integral number of 6-bit groups
			if (fewerThan24bits == EIGHTBIT) {
				b1 = binaryData[dataIndex];
				k = (byte) (b1 & 0x03);
				
				byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
						: (byte) ((b1) >> 2 ^ 0xc0);
				encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[k << 4];
				encodedData[encodedIndex++] = PAD;
				encodedData[encodedIndex++] = PAD;
			} else if (fewerThan24bits == SIXTEENBIT) {
				b1 = binaryData[dataIndex];
				b2 = binaryData[dataIndex + 1];
				l = (byte) (b2 & 0x0f);
				k = (byte) (b1 & 0x03);

				byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
						: (byte) ((b1) >> 2 ^ 0xc0);
				byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4)
						: (byte) ((b2) >> 4 ^ 0xf0);

				encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
				encodedData[encodedIndex++] = lookUpBase64Alphabet[l << 2];
				encodedData[encodedIndex++] = PAD;
			}

			return new String(encodedData);
		}

		/**
		 * Decodes Base64 data into octects
		 * 
		 * @param encoded
		 *            string containing Base64 data
		 * @return Array containind decoded data.
		 */
		public static byte[] decode(String encoded) {

			if (encoded == null) {
				return null;
			}

			char[] base64Data = encoded.toCharArray();
			// remove white spaces
			int len = removeWhiteSpace(base64Data);

			if (len % FOURBYTE != 0) {
				return null;// should be divisible by four
			}

			int numberQuadruple = (len / FOURBYTE);

			if (numberQuadruple == 0) {
				return new byte[0];
			}

			byte[] decodedData = null;
			byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
			char d1 = 0, d2 = 0, d3 = 0, d4 = 0;

			int i = 0;
			int encodedIndex = 0;
			int dataIndex = 0;
			decodedData = new byte[(numberQuadruple) * 3];

			for (; i < numberQuadruple - 1; i++) {

				if (!isData((d1 = base64Data[dataIndex++]))
						|| !isData((d2 = base64Data[dataIndex++]))
						|| !isData((d3 = base64Data[dataIndex++]))
						|| !isData((d4 = base64Data[dataIndex++]))) {
					return null;
				}// if found "no data" just return null

				b1 = base64Alphabet[d1];
				b2 = base64Alphabet[d2];
				b3 = base64Alphabet[d3];
				b4 = base64Alphabet[d4];

				decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
				decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
			}

			if (!isData((d1 = base64Data[dataIndex++]))
					|| !isData((d2 = base64Data[dataIndex++]))) {
				return null;// if found "no data" just return null
			}

			b1 = base64Alphabet[d1];
			b2 = base64Alphabet[d2];

			d3 = base64Data[dataIndex++];
			d4 = base64Data[dataIndex++];
			if (!isData((d3)) || !isData((d4))) {// Check if they are PAD characters
				if (isPad(d3) && isPad(d4)) {
					if ((b2 & 0xf) != 0)// last 4 bits should be zero
					{
						return null;
					}
					byte[] tmp = new byte[i * 3 + 1];
					System.arraycopy(decodedData, 0, tmp, 0, i * 3);
					tmp[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
					return tmp;
				} else if (!isPad(d3) && isPad(d4)) {
					b3 = base64Alphabet[d3];
					if ((b3 & 0x3) != 0)// last 2 bits should be zero
					{
						return null;
					}
					byte[] tmp = new byte[i * 3 + 2];
					System.arraycopy(decodedData, 0, tmp, 0, i * 3);
					tmp[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
					tmp[encodedIndex] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
					return tmp;
				} else {
					return null;
				}
			} else { // No PAD e.g 3cQl
				b3 = base64Alphabet[d3];
				b4 = base64Alphabet[d4];
				decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
				decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);

			}

			return decodedData;
		}

		/**
		 * remove WhiteSpace from MIME containing encoded Base64 data.
		 * 
		 * @param data
		 *            the byte array of base64 data (with WS)
		 * @return the new length
		 */
		private static int removeWhiteSpace(char[] data) {
			if (data == null) {
				return 0;
			}

			// count characters that's not whitespace
			int newSize = 0;
			int len = data.length;
			for (int i = 0; i < len; i++) {
				if (!isWhiteSpace(data[i])) {
					data[newSize++] = data[i];
				}
			}
			return newSize;
		}
	}

}
