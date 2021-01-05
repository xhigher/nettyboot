package com.nettyboot.rpcserver;

import com.nettyboot.rpcmessage.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHandler extends SimpleChannelInboundHandler<SimpleMessage> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHandler.class);

    private final HandlerContext handlerContext;

	public SimpleHandler(){
		this.handlerContext = null;
	}

    public SimpleHandler(HandlerContext handlerContext){
		this.handlerContext = handlerContext;
	}

    @Override
    public void channelRead0(final ChannelHandlerContext context, final SimpleMessage message) throws Exception {
    	logger.debug("channelRead0: message = {}", message.toString());
		if(message != null) {
			if(this.handlerContext != null){
				if(this.handlerContext.checkHeartBeat()){
					if(!MessageHelper.isHeartBeatMessage(message.getType())) {
						this.handlerContext.handleMessage(context, message);
					}else {
						context.writeAndFlush(message);
					}
				}else{
					this.handlerContext.handleMessage(context, message);
				}
			}

		}
    }

    protected void sendMessage(ChannelHandlerContext context, SimpleMessage message){
		context.writeAndFlush(message);
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        logger.error("exceptionCaught", cause);
    }
}
