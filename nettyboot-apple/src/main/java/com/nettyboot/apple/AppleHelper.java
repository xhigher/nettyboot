package com.nettyboot.apple;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwk.Jwk;
import com.nettyboot.http.HttpClientHelper;
import io.jsonwebtoken.*;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public final class AppleHelper {

    private static final Logger logger = LoggerFactory.getLogger(AppleHelper.class);

	// 购买凭证验证地址
	private static final String SANDBOX_VERIFY_RECEIPT = "https://sandbox.itunes.apple.com/verifyReceipt";
	
	private static final String VERIFY_RECEIPT = "https://buy.itunes.apple.com/verifyReceipt";

    private static final String AUTH_KEYS = "https://appleid.apple.com/auth/keys";

    private static boolean isSandBox = false;

    public static void init(Properties properties){
        isSandBox = Integer.parseInt(properties.getProperty("apple.sandbox", "0")) == 1;
    }
    /**
     * 苹果服务器验证(苹果正式环境)
     *
     * @param receipt 账单(客户端需验证字符串)
     *                返回：验证结果 json中状态码status
     *                0:表示验证成功
     *                21000 App Store无法读取你提供的JSON数据
     *                21002 收据数据不符合格式
     *                21003 收据无法被验证
     *                21004 你提供的共享密钥和账户的共享密钥不一致
     *                21005 收据服务器当前不可用
     *                21006 收据是有效的，但订阅服务已经过期。当收到这个信息时，解码后的收据信息也包含在返回内容中
     *                21007 收据信息是测试用（sandbox），但却被发送到产品环境中验证
     *                21008 收据信息是产品环境中使用，但却被发送到测试环境中验证
     */
    public static JSONObject verifyReceipt(String receipt) {
        JSONObject data = new JSONObject();
        data.put("receipt-data", receipt);
        try {
        	String resultString = HttpClientHelper.post(isSandBox ? SANDBOX_VERIFY_RECEIPT : VERIFY_RECEIPT, data);
            System.out.println("resultString = " + resultString);
        	JSONObject resultData = JSONObject.parseObject(resultString);
        	return resultData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getUserInfo(String jwt, String subject) {
        try {
            String urlData = HttpClientHelper.get(AUTH_KEYS, Collections.emptyMap());
            JSONObject keysInfos = JSONObject.parseObject(urlData);
            JSONObject keysInfo = keysInfos.getJSONArray("keys").getJSONObject(0);
            Jwk jwa = Jwk.fromValues(keysInfo);
            PublicKey publicKey = jwa.getPublicKey();
            String[] values = jwt.split("\\.");
            if (values.length < 1){
                logger.error("Invalid input parameter `jwt` = {}", jwt);
                return null;
            }
            byte[] bytes = Base64.decodeBase64(values[1]);
            JSONObject clientInfo = JSONObject.parseObject(new String(bytes));
            String audience = clientInfo.getString("aud");
            String sub = clientInfo.getString("sub");
            if (!sub.equals(subject)){
                logger.error("The sub is not equals to subject.");
                return null;
            }
            JwtParser jwtParser = Jwts.parser().setSigningKey(publicKey);
            jwtParser.requireIssuer("https://appleid.apple.com");
            jwtParser.requireAudience(audience);
            jwtParser.requireSubject(subject);
            Jws<Claims> claim = jwtParser.parseClaimsJws(jwt);
            if (claim != null && claim.getBody().containsKey("auth_time")) {
                Set<Entry<String, Object>> entries = claim.getBody().entrySet();
                JSONObject userInfo = new JSONObject();
                for (Entry<String, Object> entry : entries) {
                    userInfo.put(entry.getKey(), entry.getValue());
                }
                if (userInfo.getString("sub").equals(subject)){
                    return userInfo;
                }
            }
            return null;
        } catch (ExpiredJwtException e) {
            logger.error("apple identityToken expired", e);
            return null;
        } catch (Exception e) {
            logger.error("apple identityToken illegal", e);
            return null;
        }
    }

}
