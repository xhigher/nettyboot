package com.nettyboot.util;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public class StringUtil {
	
	private static final int earthRadius = 6378137;
	
	private static final String realnameFormatRegex = ".*([\\d\\D]{1})";
	private static final String phonenoFormatRegex = "(\\d{3})\\d+(\\d{2})";
	private static final String idcardnoFormatRegex = "(\\d{2})\\d+(\\d{2})";

	private static final String __numberChars = "0123456789";
	private static final String __diffChars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
	private static final String __randChars = "0123456789abcdefghigklmnopqrstuvtxyzABCDEFGHIGKLMNOPQRSTUVWXYZ";

	public static final Charset ENCODING_UTF8 = StandardCharsets.UTF_8;

	private final static Random __random = new Random(System.currentTimeMillis());

	private final static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static boolean isNullOrEmpty(String value) {
		return (value == null || value.isEmpty());
	}

	public static String randomNumbers(int length) {
		StringBuilder hash = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			hash.append(__numberChars.charAt(__random.nextInt(10)));
		}
		return hash.toString();
	}

	public static String randomString(int length, boolean isLowerCase) {
		int size = isLowerCase ? 36 : 62;
		StringBuilder hash = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			hash.append(__randChars.charAt(__random.nextInt(size)));
		}
		return hash.toString();
	}

	public static long randomLong(long min, long max) {
		return min + (long) (Math.random() * (max - min));
	}

	public final static String md5(String s) {
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			return toHex(md);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public final static String getCheckcode(int length){
		int size = __diffChars.length();
		StringBuilder hash = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			hash.append(__diffChars.charAt(__random.nextInt(size)));
		}
		return hash.toString();
	}

	public final static String getMsgcode(int length){
		return randomNumbers(length);
	}

	public static int randomInt(int min, int max) {
		return min + (int) (Math.random() * (max - min));
	}

	public static int FNVHash1(String data){
		final int p = 16777619;
		int hash = (int)2166136261L;
		for(int i=0;i<data.length();i++)
			hash = (hash ^ data.charAt(i)) * p;
		hash += hash << 13;
		hash ^= hash >> 7;
		hash += hash << 3;
		hash ^= hash >> 17;
		hash += hash << 5;
		return hash;
	}

	public static String hashNum(String data, int range){
		CRC32 crc32 = new CRC32();
		crc32.update(data.getBytes());
		long hash = crc32.getValue();
		hash = hash % range;
		return String.valueOf(hash);
	}

	public static String hashTableId(String data, int range){
		CRC32 crc32 = new CRC32();
		crc32.update(data.getBytes());
		long hash = crc32.getValue();
		hash = hash % range + 1;
		return String.valueOf(hash);
	}

	public static String hashTableId(long data, int range){
		long hash = (data % range + 1);
		return String.valueOf(hash);
	}

	public static String getHmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
		byte[] keyBytes = encryptKey.getBytes(ENCODING_UTF8);
		byte[] textBytes = encryptText.getBytes(ENCODING_UTF8);
		SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(secretKey);
		return Base64.getEncoder().encodeToString(mac.doFinal(textBytes));
	}

	private static String toHex(byte[] src) {
		int len = src.length;
		char[] chs = new char[len * 2];
		int j = 0;
		for (int i = 0; i < len; i++) {
			chs[j++] = hexDigits[src[i] >> 4 & 0xF];
			chs[j++] = hexDigits[src[i] >> 0 & 0xF];
		}
		return new String(chs);
	}

	public static String encodeBySHA1(String str) {
		try{
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			sha1.update(str.getBytes());
			byte[] digest = sha1.digest();
			return toHex(digest);
		}catch(Exception e){
			return null;
		}
	}

	private static final String IV_STRING = "eDeSDFX73hwxi7Xg";

	public static String encryptAESData(String privateKey, String text) {
		try{
			SecretKeySpec secretKey = new SecretKeySpec(privateKey.getBytes(ENCODING_UTF8), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] initParam = IV_STRING.getBytes(ENCODING_UTF8);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
			return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes(ENCODING_UTF8)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	public static String decryptAESData(String privateKey, String encryptText) {
		try{
			SecretKeySpec secretKey = new SecretKeySpec(privateKey.getBytes(ENCODING_UTF8), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] initParam = IV_STRING.getBytes(ENCODING_UTF8);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
			return new String(cipher.doFinal(Base64.getDecoder().decode(encryptText)), ENCODING_UTF8);
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static String encryptByUserid(String userid,String text) {
		return encryptAESData(userid+"1005", text);
	}

	public static String decryptByUserid(String userid,String text) {
		return decryptAESData(userid+"1005", text);
	}

	public static int getDistance(double lng1,double lat1,double lng2,double lat2){ 
		double radLat1 = lat1 * Math.PI / 180.0;  
		double radLat2 = lat2 * Math.PI / 180.0;  
	    double sinLat = Math.sin((radLat1 - radLat2) / 2.0);  
	    double sinLng = Math.sin(((lng1 - lng2) * Math.PI / 180.0) / 2.0);  
	    double distance = 2 * earthRadius * Math.asin(Math.sqrt(sinLat * sinLat + Math.cos(radLat1) * Math.cos(radLat2) * sinLng * sinLng));  
	    return (int)Math.round(distance);
	}
	
	public static Map<String, Integer> sortMapESC(Map<String, Integer> orignalMap) {
		Map<String, Integer> resultMap = new LinkedHashMap<String, Integer>();
		if (orignalMap != null && !orignalMap.isEmpty()) {
			List<Entry<String, Integer>> entryList = new ArrayList<Entry<String, Integer>>(orignalMap.entrySet());
			Collections.sort(entryList,new Comparator<Entry<String, Integer>>() {
						public int compare(Entry<String, Integer> entry1,Entry<String, Integer> entry2) {
							return entry1.getValue().compareTo(entry2.getValue());
						}
					});
			Iterator<Entry<String, Integer>> iter = entryList.iterator();
			Entry<String, Integer> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				resultMap.put(tmpEntry.getKey(), tmpEntry.getValue());
			}
		}
		return resultMap;
	}
	
	public static String getUrldecode(String str){
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
	
	public final static Map<String, Integer> sortMapDESC(Map<String, Integer> orignalMap) {
		Map<String, Integer> resultMap = new LinkedHashMap<String, Integer>();
		if (orignalMap != null && !orignalMap.isEmpty()) {
			List<Entry<String, Integer>> entryList = new ArrayList<Entry<String, Integer>>(orignalMap.entrySet());
			Collections.sort(entryList, new Comparator<Entry<String, Integer>>() {
						public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
							return entry2.getValue().compareTo(entry1.getValue());
						}
					});
			Iterator<Entry<String, Integer>> iter = entryList.iterator();
			Entry<String, Integer> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				resultMap.put(tmpEntry.getKey(), tmpEntry.getValue());
			}
		}
		return resultMap;
	}
	
	public static boolean checkCheckcode(String checkcode){
		Pattern pattern = Pattern.compile("^([0-9a-zA-Z]{4})$");
		Matcher matcher = pattern.matcher(checkcode);
		return matcher.matches();
	}
	
	public static boolean checkMsgcode(String msgcode){
		Pattern pattern = Pattern.compile("^(\\d{4}|\\d{6})$");
		Matcher matcher = pattern.matcher(msgcode);
		return matcher.matches();
	}
	
	public static String specialUrlEncode(String value) throws Exception {
	    return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
	}
	
	public static String formatMoney(int money){
		String moneyStr = String.valueOf(money);
		int len = moneyStr.length();
		if(len > 2){
			if(moneyStr.substring(len-2).equals("00")){
				return moneyStr.substring(0, len-2);
			}
			return moneyStr.substring(0, len-2) + "." + moneyStr.substring(len-2);
		}else if(len == 2){
			return "0." + money;
		}else if(len == 1){
			return "0.0" + money;
		}else{
			return "0.00";
		}
	} 
	
	public static String convertIdcard(String idcard) {
		if(idcard != null) {
			int len = idcard.length();
			if(len == 15) {
				int[] ratios = {7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2};
				String[] lasts = {"1","0","X","9","8","7","6","5","4","3","2"};
				int sum = 0;
				String ch = "";
				StringBuffer idcard2 = new StringBuffer();
				for(int i=0; i<17;i++) {
					if(i < 6) {
						ch = idcard.substring(i,i+1);
					}else if(i==6){
						ch = "1";
					}else if(i==7){
						ch = "9";
					}else {
						ch = idcard.substring(i-2,i-1);
					}
					sum = sum + Integer.valueOf(ch) * ratios[i];
					idcard2.append(ch);
				}
				int li = sum % 11;
				idcard2.append(lasts[li]);
				idcard = idcard2.toString(); 
			}else if(len == 18) {
				idcard = idcard.substring(0, 6) + idcard.substring(8, 17);
			}
		}
		return idcard;
	}
	
	public static String repeatString(String str, int len) {
		if(len > 0) {
			return new String(new char[len]).replace("\0", str);
		}
		return str;
	}
	
    public static String formatPhoneNo(String phoneno) {
    	return phoneno.replaceAll(phonenoFormatRegex, "$1"+ StringUtil.repeatString("*", phoneno.length()-5)+"$2");
    }
    
    public static String formatIdcardNo(String idcardno) {
    	return idcardno.replaceAll(idcardnoFormatRegex, "$1"+ StringUtil.repeatString("*", idcardno.length()-4)+"$2");
    }
    
    public static String formatRealname(String realname) {
    	return realname.replaceAll(realnameFormatRegex, "**$1");
    }
    
    public static String formatBankcardNo(String bankcardno) {
    	return StringUtil.repeatString("*", bankcardno.length()-4) + bankcardno.substring(bankcardno.length()-4);
    }
    
	public static boolean checkPhoneNo(String phoneno){
		Pattern pattern = Pattern.compile("^(1[0-9]{10})$");
		Matcher matcher = pattern.matcher(phoneno);
		return matcher.matches();
	}
	
	public static boolean checkIdCardNo(String idcardno){
		Pattern pattern18 = Pattern.compile("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$");
		Pattern pattern15 = Pattern.compile("^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$");
		return pattern18.matcher(idcardno).matches() || pattern15.matcher(idcardno).matches();
	}

	public static boolean isEmptyOrWhitespaceOnly(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}

		int length = str.length();

		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}

		return true;
	}
	
}
