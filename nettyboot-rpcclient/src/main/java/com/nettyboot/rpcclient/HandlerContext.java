package com.nettyboot.rpcclient;

import com.nettyboot.rpcmessage.SimpleMessage;

public interface HandlerContext {

    void handleMessage(final CallbackContext context, final SimpleMessage message);
}
