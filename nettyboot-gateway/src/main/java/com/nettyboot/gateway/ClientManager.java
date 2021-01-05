package com.nettyboot.gateway;

import com.nettyboot.config.LogicResultHelper;
import com.nettyboot.config.RequestInfo;
import com.nettyboot.rpcclient.CallbackContext;
import com.nettyboot.rpcclient.CallbackPool;
import com.nettyboot.rpcclient.HandlerContext;
import com.nettyboot.rpcclient.SimpleClient;
import com.nettyboot.rpcmessage.MessageType;
import com.nettyboot.rpcmessage.RequestMessageData;
import com.nettyboot.rpcmessage.SimpleMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientManager {

	private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);
	
	public static final long REQUEST_TIMEOUT_MILLIS = 5000;

    private static final ScheduledExecutorService requestTimeoutWatcher = Executors.newSingleThreadScheduledExecutor();

    private static boolean initOK = false;

    private static HandlerContext clientContext;

    public static void init(Properties properties){
        if(!initOK) {

            clientContext = new HandlerContext() {
                @Override
                public void handleMessage(CallbackContext context, SimpleMessage message) {
                    ClientManager.sendResult(context.getChannel(), context.getAllowOrigin(), message.getData().toString());
                }
            };

            requestTimeoutWatcher.scheduleAtFixedRate(new RequestTimeoutTask(), REQUEST_TIMEOUT_MILLIS, REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            ServiceManager.init(properties);

            initOK = true;
        }
    }

    protected static SimpleClient getClient(String host, int port){
        SimpleClient client = new SimpleClient(clientContext, host, port);
        return client;
    }

    public static void sendResult(Channel channel, String allowOrigin, String responseContent){
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,Unpooled.wrappedBuffer(responseContent.getBytes()));
		if(responseContent != null && !responseContent.isEmpty()){
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=" + CharsetUtil.UTF_8.toString());
		}else{
			response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html; charset=" + CharsetUtil.UTF_8.toString());
		}
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
		if (allowOrigin != null) {
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);
		}
		channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    
    public static void submit(Channel channel, String allowOrigin, RequestInfo requestInfo){
        try{
            SimpleClient client = ServiceManager.getClient(requestInfo.getModule(), requestInfo.getAction(), requestInfo.getVersion());
            if(client == null){
            	sendResult(channel, allowOrigin, LogicResultHelper.errorRequest());
            	return;
            }

            RequestMessageData messageData = new RequestMessageData();
            messageData.setModule(requestInfo.getModule());
            messageData.setAction(requestInfo.getAction());
            messageData.setVersion(requestInfo.getVersion());
            messageData.setParameters(requestInfo.getParameters());
            SimpleMessage message = new SimpleMessage(MessageType.request);
            message.setData(messageData);
            
            client.sendMessage(channel, allowOrigin, message, REQUEST_TIMEOUT_MILLIS);

        }catch(Exception e){
        	logger.error("callService.Exception", e);
        	sendResult(channel, allowOrigin, LogicResultHelper.errorInternal());
        }
    }

    public static void release() {
        requestTimeoutWatcher.shutdown();
        ServiceManager.release();
    }
    
    static class RequestTimeoutTask implements Runnable {

        @Override
        public void run() {
            check();
        }

        private synchronized void check() {
            try {
            	int requestCount = 0;
            	int timeoutCount = 0;
                List<CallbackContext> requestList = CallbackPool.getRequestList();
                requestCount = requestList.size();
                if(requestCount > 0) {
                	for (CallbackContext context : requestList) {
                        if (System.currentTimeMillis() > context.getTimeoutMillis()) {
                        	CallbackPool.remove(context.getRequestId());
                        	ClientManager.sendResult(context.getChannel(), context.getAllowOrigin(), LogicResultHelper.errorInternal());
                        	timeoutCount ++;
                        }
                    }
                    logger.info("RequestTimeoutTask.check: requestCount={}, timeoutCount={}", requestCount, timeoutCount);
                }
            } catch (Exception e) {
            	logger.warn("RequestTimeoutTask.Exception", e);
            }
        }
    }
}

