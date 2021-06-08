package com.nettyboot.biznode;

import com.nettyboot.rpcserver.SimpleServer;

import java.util.Properties;

public class BiznodeServer extends SimpleServer {

    public BiznodeServer(Properties properties) {
        super(properties, new BiznodeHandlerContext());
    }

    @Override
    protected void init(Properties properties){
        XLogicManager.init(properties);
    }

    @Override
    public void stop() {
        super.stop();

        XLogicManager.release();
    }
}
