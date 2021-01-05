package com.nettyboot.rpcserver;

import com.nettyboot.rpcmessage.MessageDecoder;
import com.nettyboot.rpcmessage.MessageEncoder;
import com.nettyboot.server.BaseServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public class SimpleServer extends BaseServer {

	private static final Logger logger = LoggerFactory.getLogger(SimpleServer.class);
	
	private final String serverHost = "0.0.0.0";
	private final int serverPort;
	private final int workerThreads;
	private final int businessThreads;
	private final int maxFrameLength;

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
	protected ServerBootstrap bootstrap = null;
	private DefaultEventExecutorGroup executorGroup = null;
    
    private final int soRcvbuf = 1024 * 128;
    private final int soSndbuf = 1024 * 128;

    private final HandlerContext handlerContext;

	public SimpleServer(Properties properties) {
		this(properties, null);
	}
    
	public SimpleServer(Properties properties, HandlerContext handlerContext) {
		this.handlerContext = handlerContext;
		serverPort = Integer.parseInt(properties.getProperty("server.port", "9800").trim());
		workerThreads = Integer.parseInt(properties.getProperty("server.workerThreads", "4").trim());
		businessThreads = Integer.parseInt(properties.getProperty("server.businessThreads","0").trim());
		maxFrameLength = Integer.parseInt(properties.getProperty("server.maxFrameLength", "1048576").trim());

        this.init(properties);
	}

	@Override
	protected void init(Properties properties){

	}

	@Override
	public void start() {
		if(businessThreads > 0) {
			executorGroup = new DefaultEventExecutorGroup(businessThreads, new DefaultThreadFactory("business-thread"));
		}
		bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(workerThreads);
		try {
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel channel) {
					ChannelPipeline pipeline = channel.pipeline();
					pipeline.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength, 0, 4, 0, 0));
					pipeline.addLast(new MessageDecoder());
					pipeline.addLast(new MessageEncoder());
					if(executorGroup != null){
						pipeline.addLast(executorGroup, "", createHandler());
					}else{
						pipeline.addLast(createHandler());
					}
				}
			});
			bootstrap.option(ChannelOption.SO_BACKLOG, 128);
			bootstrap.option(ChannelOption.SO_RCVBUF, soRcvbuf);
			bootstrap.option(ChannelOption.SO_SNDBUF, soSndbuf);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

	        ChannelFuture future = bootstrap.bind(serverHost, serverPort).sync();
	        future.channel().closeFuture().sync();

		}catch (Exception e){
			logger.error("XServer.start.Exception",e);
		} finally {
			stop();
		}
	}

	protected SimpleHandler createHandler(){
		if(this.handlerContext != null){
			return new SimpleHandler(this.handlerContext);
		}
		return new SimpleHandler();
	}

	@Override
	public void stop() {
		if(executorGroup != null){
			executorGroup.shutdownGracefully();
		}
		if(bossGroup!=null){
			bossGroup.shutdownGracefully();
		}
		if(workerGroup != null){
			workerGroup.shutdownGracefully();
		}
	}

}
