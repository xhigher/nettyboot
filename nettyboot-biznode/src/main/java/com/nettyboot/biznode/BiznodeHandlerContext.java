package com.nettyboot.biznode;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.RequestInfo;
import com.nettyboot.rpcmessage.MessageHelper;
import com.nettyboot.rpcmessage.MessageType;
import com.nettyboot.rpcmessage.SimpleMessage;
import com.nettyboot.rpcserver.HandlerContext;
import io.netty.channel.ChannelHandlerContext;

public class BiznodeHandlerContext extends HandlerContext {

	@Override
	public void handleMessage(ChannelHandlerContext context, SimpleMessage message) {
		SimpleMessage response = new SimpleMessage(MessageType.response, message.getMsgid());
		response.setData(XLogicManager.executeLogic(convertToRequestInfo(message)));
		context.writeAndFlush(message);
	}

	public RequestInfo convertToRequestInfo(SimpleMessage message) {
		return JSONObject.toJavaObject((JSONObject)message.getData(), RequestInfo.class);
	}
}
