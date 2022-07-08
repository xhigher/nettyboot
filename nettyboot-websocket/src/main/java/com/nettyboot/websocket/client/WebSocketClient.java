package com.nettyboot.websocket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class WebSocketClient extends Thread {

    // 服务端IP地址
    private String host;
    // 服务端监听端口号
    private int port;
    // 通信管道
    private Channel channel;

    public WebSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            connectServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectServer() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {

            WebSocketHandler handler = new WebSocketHandler();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
                            pipeline.addLast(handler);
                        }
                    });
            // 发起异步连接操作
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
            System.out.println("客户端已启动");
            // 等待客户端链路关闭
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("连接失败，服务端是否开启？");
        } finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

    public void sendMessage(String msg) {
		if (channel != null && channel.isActive()) {
            System.out.println("发送消息："+msg);
            WebSocketFrame frame = new TextWebSocketFrame(msg);
			channel.writeAndFlush(frame);
		} else {
			System.out.println("未建立连接，不能发送消息");
		}
	}

}
