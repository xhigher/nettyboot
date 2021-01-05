package com.nettyboot.rpcmessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageEncoder extends MessageToByteEncoder<SimpleMessage> {
	
	private static final Logger logger = LoggerFactory.getLogger(MessageEncoder.class);
	
    @Override
    public void encode(ChannelHandlerContext ctx, SimpleMessage data, ByteBuf out) throws Exception {
    	try{
			byte[] bytes = data.toString().getBytes();
			out.writeInt(bytes.length);
			out.writeBytes(bytes);
    	}catch(Exception e){
    		logger.error("MessageEncoder.encode", e);
    	}
    }
}
