package com.nettyboot.websocket.server;

import com.nettyboot.logic.LogicManager;
import com.nettyboot.server.BaseServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/*
 * @copyright (c) xhigher 2015
 * @author xhigher    2015-3-26 
 */
public class WebSocketServer extends BaseServer {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

	private final String serverHost = "0.0.0.0";
	private final int serverPort;
	private final int workerThreads;
	private final int businessThreads;
	private final int maxContentLength;
	private final int readerIdleTime;
	private final int writerIdleTime;
	private final int allIdleTime;


	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	protected ServerBootstrap bootstrap = null;
	private DefaultEventExecutorGroup executorGroup = null;

	public WebSocketServer(Properties properties) {

		serverPort = Integer.parseInt(properties.getProperty("server.port","8080").trim());
		workerThreads = Integer.parseInt(properties.getProperty("server.workerThreads", "4").trim());
		businessThreads = Integer.parseInt(properties.getProperty("server.businessThreads","0").trim());
		maxContentLength = Integer.parseInt(properties.getProperty("server.maxContentLength", "65536").trim());
		readerIdleTime = Integer.parseInt(properties.getProperty("server.readerIdleTime", "30").trim());
		writerIdleTime = Integer.parseInt(properties.getProperty("server.writerIdleTime", "0").trim());
		allIdleTime = Integer.parseInt(properties.getProperty("server.allIdleTime", "0").trim());

		this.init(properties);
	}

	@Override
	protected void init(Properties properties){
		LogicManager.init(properties.getProperty("logic.package", "").trim());
	}


	@Override
	public void start() {
		if(businessThreads > 0){
			executorGroup = new DefaultEventExecutorGroup(businessThreads, new DefaultThreadFactory("biz-thread"));
		}
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
		try {
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.option(ChannelOption.SO_BACKLOG, 100);
			bootstrap.handler(new LoggingHandler(LogLevel.INFO));
			bootstrap.childHandler(new XChannelInitializer(executorGroup));

			System.out.println("服务端开启等待客户端连接: "+serverHost+":"+serverPort);
			ChannelFuture future = bootstrap.bind(serverHost,serverPort).sync();
			future.channel().closeFuture().sync();

		}catch (Exception e){
			logger.error("XServer.start.Exception",e);
		} finally {
			stop();
		}
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

	class XChannelInitializer extends ChannelInitializer<SocketChannel> {

		private final DefaultEventExecutorGroup executorGroup;

		public XChannelInitializer(final DefaultEventExecutorGroup executorGroup) {
			this.executorGroup = executorGroup;
		}

		@Override
		public void initChannel(SocketChannel ch) {
			ChannelPipeline pipeline = ch.pipeline();
			//pipeline.addLast(new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.SECONDS));

			pipeline.addLast(new HttpServerCodec());
			pipeline.addLast(new HttpObjectAggregator(maxContentLength));
			pipeline.addLast(new WebSocketServerCompressionHandler());
			pipeline.addLast(new WebSocketServerProtocolHandler("/", null,true));
			if(this.executorGroup != null){
				pipeline.addLast(this.executorGroup, new WebSocketHandler());
			}else{
				pipeline.addLast(new WebSocketHandler());
			}
		}
	}

}
