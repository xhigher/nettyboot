package com.nettyboot.webserver;

import com.nettyboot.logic.LogicManager;
import com.nettyboot.redis.XRedis;
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
public class WebDefaultServer extends WebBaseServer {

	public WebDefaultServer(Properties properties) {
		super(properties);
	}

	@Override
	protected void init(Properties properties){
		LogicManager.init(properties.getProperty("logic.package").trim());
	}


	@Override
	protected WebBaseHandler newHandler(){
		return new WebDefaultHandler();
	}

}
