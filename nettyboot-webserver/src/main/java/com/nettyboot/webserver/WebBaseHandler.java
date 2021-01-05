package com.nettyboot.webserver;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.BaseConfig.PostBodyType;
import com.nettyboot.config.RequestInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;

public abstract class WebBaseHandler extends SimpleChannelInboundHandler<HttpObject> {

	protected static Logger logger = LoggerFactory.getLogger(WebBaseHandler.class);

	protected HttpMethod requestMethod = null;

	private StringBuffer postBody = null;

	protected  String allowOrigin;
	protected final RequestInfo requestInfo;

	public WebBaseHandler(){
		requestInfo = new RequestInfo();
	}

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
        	HttpRequest request = (HttpRequest) msg;
        	
        	if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }
        	
        	DecoderResult result = request.decoderResult();
        	if (!result.isSuccess()) {
        		sendError(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
        	
        	HttpHeaders header = request.headers();
        	allowOrigin = header.get(HttpHeaderNames.ORIGIN);
        	if(allowOrigin != null){
    			if(!WebConfig.checkAllowOrigin(allowOrigin)){
    				allowOrigin = null;
    			}
    		}

			requestMethod = request.method();
            if (requestMethod == HttpMethod.OPTIONS) {
            	sendOptionsResult(ctx);
                return;
            }
            
        	if (!WebConfig.checkAllowMethods(requestMethod)) {
                sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
                return;
            }

        	QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        	String requestPath = decoder.path();
        	logger.info("{}: {}", request.method(), requestPath);
            if (requestPath == null) {
                sendError(ctx, HttpResponseStatus.FORBIDDEN);
                return;
            }
            
            Matcher matcher = WebConfig.checkRequestPath(requestPath);
    		if(!matcher.matches()){
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
                return;
    		}

			requestInfo.setVersion(Integer.parseInt(matcher.group(1)));
			requestInfo.setModule(matcher.group(2));
			requestInfo.setAction(matcher.group(3));

			WebConfig.parseHeaders(header, requestInfo);

			String clientIP = requestInfo.getClientIP();
			if(clientIP == null || clientIP.isEmpty()){
				InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
				clientIP = insocket.getAddress().getHostAddress();
				requestInfo.setClientIP(clientIP);
			}

			requestInfo.addParameters(decoder.parameters());

        }
       
        if (msg instanceof HttpContent) {
        	HttpContent chunk = (HttpContent) msg;
        	
        	if(requestMethod == HttpMethod.POST){
	        	ByteBuf content = chunk.content();
	            if (content.isReadable()) {
	            	if(postBody == null){
						postBody = new StringBuffer();
					}
					postBody.append(content.toString(CharsetUtil.UTF_8));
	            }
        	}

			if (chunk instanceof LastHttpContent) {
				handleBusiness(ctx);
			}
        }else{
        	if(msg instanceof LastHttpContent){
        		handleBusiness(ctx);
        	}
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.error("exceptionCaught:", cause);
    	ctx.flush();
    	ctx.channel().close();
    }
    
    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);
        ctx.write(response);
    }
    
    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset="+CharsetUtil.UTF_8.toString());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    
 
    private void sendOptionsResult(ChannelHandlerContext ctx){
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		if (allowOrigin != null) {
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);
			String allowMethods = WebConfig.getAccessControlAllowMethods();
			if(allowMethods != null && !allowMethods.isEmpty()){
				response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, allowMethods);
			}
			String allowHeaders = WebConfig.getAccessControlAllowHeaders();
			if(allowHeaders != null && !allowHeaders.isEmpty()){
				response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
			}
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    
    protected void sendResult(ChannelHandlerContext ctx, String responseContent){
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,Unpooled.wrappedBuffer(responseContent.getBytes()));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE,WebConfig.getResponseContentType());
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
		if (allowOrigin != null) {
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    protected void parsePostBody(){
		if(requestMethod == HttpMethod.POST && postBody != null){
			String bodyString = postBody.toString();
			PostBodyType type = WebConfig.getPostBodyType();
			if(type == PostBodyType.JSON){
				JSONObject data = JSONObject.parseObject(bodyString);
				requestInfo.addParameters(data);
			}else if (type == PostBodyType.FORM){
				QueryStringDecoder decoder = new QueryStringDecoder(bodyString);
				requestInfo.addParameters(decoder.parameters());
			}
		}
	}

	protected abstract boolean checkRequest(ChannelHandlerContext context);

	protected abstract void executeLogic(ChannelHandlerContext context);
	
	protected void handleBusiness(ChannelHandlerContext context){
		this.parsePostBody();

		if(!this.checkRequest(context)){
			return;
		}

		this.executeLogic(context);
	}
}