package com.nettyboot.rpcmessage;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(MessageHelper.class);

	public static boolean isHeartBeatMessage(MessageType type) {
		return MessageType.heartbeat == type;
	}

	public static SimpleMessage newHeartBeatMessage() {
		return new SimpleMessage(MessageType.heartbeat);
	}

	public static SimpleMessage toMessage(String data) {
		SimpleMessage message = null;
		try {
			message = JSONObject.parseObject(data, SimpleMessage.class);
		}catch(Exception e) {
			logger.error("toMessage: data = {}" + data, e);
		}
		return message;
	}

	public static RequestMessageData getRequestMessageData(SimpleMessage message) {
		return JSONObject.toJavaObject((JSONObject)message.getData(), RequestMessageData.class);
	}

	public static ResponseMessageData getResponseMessageData(SimpleMessage message) {
		return JSONObject.toJavaObject((JSONObject)message.getData(), ResponseMessageData.class);
	}
}
