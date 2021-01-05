package com.nettyboot.rpcclient;

import com.nettyboot.rpcmessage.MessageHelper;
import com.nettyboot.rpcmessage.SimpleMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends SimpleChannelInboundHandler<SimpleMessage> {
    
	private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

	private int lostEchoCount = 0;
	
	private final SimpleClient simpleClient;
	
	public ClientHandler(SimpleClient client) {
		this.simpleClient = client;
	}
	
    @Override
    public void channelRead0(ChannelHandlerContext ctx, SimpleMessage message) throws Exception {
    	logger.info("channelRead0: message = {}", message.toString());
    	if(message != null) {
    	    String msgid = message.getMsgid();
            try {
            	if(!MessageHelper.isHeartBeatMessage(message.getType())) {
            		CallbackContext context = CallbackPool.getContext(msgid);
                    if (context != null) {
                        this.simpleClient.handleMessage(context, message);
                    }else {
                    	logger.warn("Receive msg from server but no context found, msgid=" + msgid);
                    }
            	}else{
                    lostEchoCount --;
                    if(lostEchoCount < 0){
                        lostEchoCount = 0;
                    }
                }
            } finally {
            	CallbackPool.remove(msgid);
            }
    	}
    	
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state() == IdleState.READER_IDLE){
            	try {
                    lostEchoCount ++;
                    if (lostEchoCount > 2){
                    	lostEchoCount = 0;
                        this.simpleClient.tryToReconnect();
                    }else {
                        ctx.writeAndFlush(MessageHelper.newHeartBeatMessage());
                    }
            	}catch(Exception e) {
            		lostEchoCount = 0;
            		logger.error(ctx.channel().remoteAddress().toString(), e);
            		this.simpleClient.tryToReconnect();
            	}
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(ctx.channel().remoteAddress().toString(), cause);
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }



}
