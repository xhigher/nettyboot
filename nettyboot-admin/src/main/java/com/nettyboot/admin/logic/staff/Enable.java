package com.nettyboot.admin.logic.staff;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.admin.base.AdminLogic;
import com.nettyboot.admin.conf.AdminConfig;
import com.nettyboot.admin.conf.AdminDataKey;
import com.nettyboot.admin.conf.AdminRedisKeys;
import com.nettyboot.admin.conf.BaseModules;
import com.nettyboot.admin.model.admin.StaffInfoModel;
import com.nettyboot.config.BaseDataKey;
import com.nettyboot.config.ErrorCode;
import com.nettyboot.config.LogicAnnotation;
import com.nettyboot.config.LogicMethod;
import com.nettyboot.mail.MailHelper;
import com.nettyboot.redis.XRedis;
import com.nettyboot.util.StringUtil;

@LogicAnnotation(module= BaseModules.staff, action = "enable", method = LogicMethod.POST, parameters = { AdminDataKey.staffid })
public class Enable extends AdminLogic {

	private int targetStaffid = 0;

	@Override
	protected String prepare() {
		targetStaffid = this.getIntegerParameter(AdminDataKey.staffid);
		return null;
	}

	@Override
	protected String execute() {
		StaffInfoModel staffModel = new StaffInfoModel(targetStaffid);
		JSONObject staffInfo = staffModel.getInfo();
		if (staffInfo == null) {
			return errorInternalResult();
		}
		if (staffInfo.isEmpty()) {
			return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
		}

		if (staffInfo.getInteger(BaseDataKey.status) != StaffInfoModel.Status.activated) {

			String password = StringUtil.randomNumbers(8);
			if (!staffModel.enable(password, staffInfo.getString(BaseDataKey.regtime))) {
				return this.errorResult();
			}
			staffInfo = staffModel.getInfo();
			if (staffInfo == null) {
				return errorInternalResult();
			}
			if (staffInfo.isEmpty()) {
				return this.errorResult(ErrorCode.ACCOUNT_NULL, "ACCOUNT_NULL");
			}
			
			String username = staffInfo.getString(BaseDataKey.username);
			String email = staffInfo.getString(BaseDataKey.email);
			// 【球胜后台管理系统】 登录账号激活成功！
			String subject = "\u3010\u7403\u80dc\u540e\u53f0\u7ba1\u7406\u7cfb\u7edf\u3011\u0020\u767b\u5f55\u8d26\u53f7\u6fc0\u6d3b\u6210\u529f\uff01";
			StringBuffer context = new StringBuffer();

			// 您的登录账号为：
			context.append("\u60a8\u7684\u767b\u5f55\u8d26\u53f7\u4e3a\uff1a");
			context.append(username);

			// , 登录密码为：
			context.append("\u002c\u0020\u767b\u5f55\u5bc6\u7801\u4e3a\uff1a");
			context.append(password);

			// ,请勿泄漏于他人。 登录地址：http://
			context.append("\u002c\u8bf7\u52ff\u6cc4\u6f0f\u4e8e\u4ed6\u4eba\u3002\u0020\u0020\u0020\u767b\u5f55\u5730\u5740\uff1a\u0068\u0074\u0074\u0070\u003a\u002f\u002f");
			context.append(AdminConfig.BASE_URL);

			JSONObject message = new JSONObject();
			message.put("email", email);
			message.put("context", context.toString());
			message.put("subject", subject);

			MailHelper.sendMail(message);

//			if(!SMSHelper.sendAdminActivated(username, email)){
//				logger.error("SMSHelper.sendAdminActivated: username="+username+", email="+email);
//			}
			
			XRedis.del(AdminRedisKeys.STAFF_INFO.build().append(targetStaffid));
		}
		
		return this.successResult();
	}
}
