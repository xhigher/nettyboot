package com.nettyboot.admin.logic.common;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.captcha.CaptchaHelper;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.redis.RedisKey;
import com.nettyboot.redis.XRedis;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@LogicAnnotation(module= BaseModules.common, action="captcha")
public class Captcha extends AdminLogic {

	@Override
	protected boolean requireSession() {
		return false;
	}

	@Override
	protected String prepare() {
		return null;
	}

	@Override
	protected String execute() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String checkcode = CaptchaHelper.getInstance().getChallangeAndWriteImage(baos);
		String base64bytes = "data:image/png;base64,"+ Base64.getEncoder().encodeToString(baos.toByteArray());
		checkcode = checkcode.toLowerCase();
		
		RedisKey redisKey = AdminRedisKeys.CHECKCODE.build().append(this.getPeerid()).append(checkcode);
		XRedis.set(redisKey, checkcode);
		
		JSONObject resultInfo = new JSONObject();
		resultInfo.put("base64", base64bytes);
		return this.successResult(resultInfo);
	}
}
