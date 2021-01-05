package com.nettyboot.webserver;

import com.nettyboot.server.BaseServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/*
 * @copyright (c) xhigher 2015
 * @author xhigher    2015-3-26 
 */
public abstract class WebBaseServer extends BaseServer {

	private static final Logger logger = LoggerFactory.getLogger(WebBaseServer.class);
	
	private final String serverHost = "0.0.0.0";
	private final int serverPort;
	private final int workerThreads;
	private final int businessThreads;
	private final int maxContentLength;

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
	protected ServerBootstrap bootstrap = null;
	private DefaultEventExecutorGroup executorGroup = null;

	public WebBaseServer(Properties properties) {

		serverPort = Integer.parseInt(properties.getProperty("webserver.port","8080").trim());
		workerThreads = Integer.parseInt(properties.getProperty("webserver.workerThreads", "4").trim());
		businessThreads = Integer.parseInt(properties.getProperty("webserver.businessThreads","0").trim());
		maxContentLength = Integer.parseInt(properties.getProperty("webserver.maxContentLength", "1048576").trim());

		WebConfig.init(properties);

		this.init(properties);
	}

	protected abstract WebBaseHandler newHandler();

	@Override
	public void start() {
		if(businessThreads > 0){
			executorGroup = new DefaultEventExecutorGroup(businessThreads, new DefaultThreadFactory("business-thread"));
		}
		bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(workerThreads);
		try {
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.option(ChannelOption.SO_BACKLOG, 100);
			bootstrap.handler(new LoggingHandler(LogLevel.INFO));
			bootstrap.childHandler(new XChannelInitializer(executorGroup));
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
/*		pipeline.addLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
		pipeline.addLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS));*/
			pipeline.addLast(new HttpRequestDecoder());
			pipeline.addLast(new HttpResponseEncoder());
			pipeline.addLast(new HttpObjectAggregator(maxContentLength));
			pipeline.addLast(new ChunkedWriteHandler());
			if(this.executorGroup != null){
				pipeline.addLast(this.executorGroup, "", newHandler());
			}else{
				pipeline.addLast(newHandler());
			}
		}
	}

}
