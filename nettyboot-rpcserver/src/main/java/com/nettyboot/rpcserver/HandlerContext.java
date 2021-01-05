package com.nettyboot.rpcserver;

import com.nettyboot.rpcmessage.SimpleMessage;
import io.netty.channel.ChannelHandlerContext;

public abstract class HandlerContext {

    public abstract void handleMessage(final ChannelHandlerContext context, final SimpleMessage message);

    public boolean checkHeartBeat(){
        return true;
    }
}
