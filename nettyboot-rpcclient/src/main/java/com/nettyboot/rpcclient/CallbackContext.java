package com.nettyboot.rpcclient;

import io.netty.channel.Channel;

public class CallbackContext {

    private final String requestId;
    
    private final long timeoutMillis;
    
    private final Channel channel;
    
    private final String allowOrigin;
    
    public CallbackContext(String requestId, Channel channel, String allowOrigin, long timeout) {
        this.requestId = requestId;
        this.channel = channel;
        this.allowOrigin = allowOrigin;
        this.timeoutMillis = System.currentTimeMillis() + timeout;
    }

    public CallbackContext(String requestId, long timeout) {
        this.requestId = requestId;
        this.channel = null;
        this.allowOrigin = null;
        this.timeoutMillis = System.currentTimeMillis() + timeout;
    }

    public String getRequestId() {
        return requestId;
    }
    
    public long getTimeoutMillis() {
    	return this.timeoutMillis;
    }

    public Channel getChannel() {
        return channel;
    }
    
    public String getAllowOrigin() {
        return allowOrigin;
    }
 
}
