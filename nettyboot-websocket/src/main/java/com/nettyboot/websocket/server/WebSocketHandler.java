package com.nettyboot.websocket.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;


public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private static Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
		logger.info("接收消息1："+msg);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		// 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
		if (evt instanceof IdleStateEvent) {
			// 强制类型转换
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
				logger.info("进入读空闲...");
				WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
				ctx.channel().writeAndFlush(frame);
			} else if (event.state() == IdleState.WRITER_IDLE) {
				logger.info("进入写空闲...");
			} else if (event.state() == IdleState.ALL_IDLE) {
				logger.info("所有的空闲...");
				Channel channel = ctx.channel();
				// 关闭无用的channel，以防资源浪费
				//channel.close();
			}
		}

	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		logger.info("接收消息2："+frame);
		if (frame instanceof TextWebSocketFrame) {

			Channel channel = ctx.channel();
			String message = (((TextWebSocketFrame) frame).text());
			logger.info("channelRead0.message="+message);
			if(message != null){
				ctx.channel().writeAndFlush(new TextWebSocketFrame("我收到了："+message));
			}else{
				logger.error("channelRead0.channel="+channel);
			}
		}else {
			throw new UnsupportedOperationException("unsupported frame type: " + frame.getClass().getName());
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		Channel channel = ctx.channel();
		logger.info("Client:" + channel.remoteAddress() + "加入");
	}


	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		Channel channel = ctx.channel();
		logger.info("Client:" + channel.remoteAddress() + "离开");
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
		Channel channel = ctx.channel();
		logger.info("Client:" + channel.remoteAddress() + "在线");
		WebSocketFrame frame = new TextWebSocketFrame("ddsssssssss");
		ctx.channel().writeAndFlush(frame);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
		Channel channel = ctx.channel();
		logger.info("Client:" + channel.remoteAddress() + "掉线");
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Channel channel = ctx.channel();
		logger.info("Client:" + channel.remoteAddress() + "异常, Cause: "+ cause);
		ctx.flush();
		channel.close();
	}


	private String getClientIP(ChannelHandlerContext ctx){
		InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
		return socketAddress.getAddress().getHostAddress();
	}
}

