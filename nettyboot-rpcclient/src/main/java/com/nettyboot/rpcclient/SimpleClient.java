package com.nettyboot.rpcclient;

import com.nettyboot.rpcmessage.MessageDecoder;
import com.nettyboot.rpcmessage.MessageEncoder;
import com.nettyboot.rpcmessage.SimpleMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class SimpleClient {

    private static final Logger logger = LoggerFactory.getLogger(SimpleClient.class);
 
    private final boolean soKeepalive = true;

    private final boolean soReuseaddr = true;

    private final boolean tcpNodelay = true;
    
    private final int connTimeout = 5000;

    private final int soRcvbuf = 1024 * 128;

    private final int soSndbuf = 1024 * 128;
    
    private final String serverIP;

    private final int serverPort;

    private final HandlerContext handlerContext;

    private final Bootstrap bootstrap;
    private final NioEventLoopGroup eventLoopGroup;
    
    private ChannelFuture channelFuture;



    public SimpleClient(HandlerContext handlerContext, String ip, int port) {
        this.handlerContext = handlerContext;
        this.serverIP = ip;
        this.serverPort = port;

        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connTimeout);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, this.soKeepalive);
        bootstrap.option(ChannelOption.SO_REUSEADDR, this.soReuseaddr);
        bootstrap.option(ChannelOption.TCP_NODELAY, this.tcpNodelay);
        bootstrap.option(ChannelOption.SO_RCVBUF, this.soRcvbuf);
        bootstrap.option(ChannelOption.SO_SNDBUF, this.soSndbuf);
        
        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline cp = ch.pipeline();
                cp.addLast(new MessageEncoder());
                cp.addLast(new LengthFieldBasedFrameDecoder(1024*1024*10, 0, 4, 0, 0));
                cp.addLast(new MessageDecoder());
                cp.addLast(new IdleStateHandler(60, 0, 0));
                cp.addLast(new ClientHandler(SimpleClient.this));
            }
        };
        eventLoopGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(serverIP.substring(serverIP.lastIndexOf(".")+1)+"-"+serverPort+"-client-ip"));
        bootstrap.group(eventLoopGroup).handler(initializer);
        
        channelFuture = this.connect();
    }

    private ChannelFuture connect() {
        try {
        	ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(serverIP, serverPort));
        	channelFuture.addListener(new ChannelFutureListener() {
        		
        		@Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                    	logger.info("Connection " + channelFuture.channel() + " is well established");
                    } else {
                    	logger.warn(String.format("Connection get failed on %s due to %s", channelFuture.cause().getMessage(), channelFuture.cause()));
                    }
                }
            });
            logger.info("connect to " + getAddress());
            return channelFuture;
        } catch (Exception e) {
        	logger.error("Failed to connect to " + getAddress(), e);
        }
        return null;
    }
    

    private ChannelFuture getChannelFuture() {
    	if(channelFuture != null) {
    		if(channelFuture.isSuccess()) {
    			if(channelFuture.channel().isActive()) {
    				return channelFuture;
    			}else {
    				channelFuture.channel().close();
    				channelFuture = this.connect();
    			}
            }
    	}else {
    		channelFuture = this.connect();
    	}
    	return null;
    }

    public void handleMessage(CallbackContext context, SimpleMessage message)  {
        handlerContext.handleMessage(context, message);
    }

    public void sendMessage(SimpleMessage message, long timeout) throws Exception {
        this.sendMessage(null, null, message, timeout);
    }
    
    public void sendMessage(Channel channel, String allowOrigin, SimpleMessage message, long timeout) throws Exception {
    	ChannelFuture channelFuture = getChannelFuture();
    	if (channelFuture != null) {
            try {
            	logger.info("sendMessage message = {}", message.toString());
                CallbackPool.put(message.getMsgid(), channel, allowOrigin, timeout);
                channelFuture.channel().writeAndFlush(message);
            } catch (Exception e) {
            	throw new SimpleException(SimpleException.ExceptionType.connection, "Failed to transport", e);
            }
        } else {
            throw new SimpleException(SimpleException.ExceptionType.internal, "Socket channel is not well established");
        }
    }
    
    public void tryToReconnect() {
    	if(channelFuture != null) {
    		if(channelFuture.isSuccess()) {
    			channelFuture.channel().close();
                logger.info("tryToReconnect close: {}", channelFuture);
            }
    	}
        channelFuture = this.connect();
        logger.info("tryToReconnect: {}", channelFuture);
    }

    public void shutdown() {
        if (eventLoopGroup != null) {
        	eventLoopGroup.shutdownGracefully();
            channelFuture = null;
        }
    }
    
    public String getAddress() {
        return serverIP + ":" + serverPort;
    }
    
    public boolean isClosed() {
    	return channelFuture==null || !channelFuture.isSuccess() || !channelFuture.channel().isActive() || this.eventLoopGroup.isShutdown();
    }

}
