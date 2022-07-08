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
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
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
import java.net.URISyntaxException;

public class WebSocketClient extends Thread {

    // 服务端IP地址
    private String host;
    // 服务端监听端口号
    private int port;
    // 服务端是否https
    private boolean httpsFlag;
    // 通信管道
    private Channel channel;
    private static final String WSS_URI_FORMAT = "wss://%s:%d";
    private static final String WS_URI_FORMAT = "ws://%s:%d";

    public WebSocketClient(String host, int port) {
        this(host, port, false);
    }

    public WebSocketClient(String host, int port, boolean httpsFlag) {
        this.host = host;
        this.port = port;
        this.httpsFlag = httpsFlag;
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
            // 握手
            handshaker(handler);
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

    private void handshaker(WebSocketHandler handler) throws URISyntaxException, InterruptedException {
        WebSocketClientHandshaker handshaker = getWebSocketClientHandshaker();
        handler.setHandshaker(handshaker);
        handshaker.handshake(channel);
        //阻塞等待是否握手成功
        handler.handshakeFuture().sync();
        System.out.println("握手成功");
    }

    private URI getWebsocketURI() throws URISyntaxException {
        return new URI(String.format(httpsFlag ? WSS_URI_FORMAT: WS_URI_FORMAT, host, port));
    }

    private WebSocketClientHandshaker getWebSocketClientHandshaker() throws URISyntaxException {
        URI websocketURI = getWebsocketURI();
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        //进行握手
        return WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String)null, true,httpHeaders);
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
