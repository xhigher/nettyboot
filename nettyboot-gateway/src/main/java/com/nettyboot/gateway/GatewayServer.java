package com.nettyboot.gateway;

import com.nettyboot.webserver.WebBaseHandler;
import com.nettyboot.webserver.WebDefaultHandler;
import com.nettyboot.webserver.WebDefaultServer;

import java.util.Properties;

public class GatewayServer extends WebDefaultServer {

    public GatewayServer(Properties properties) {
        super(properties);
    }

    @Override
    protected void init(Properties properties) {
        super.init(properties);
        ClientManager.init(properties);
    }

    @Override
    protected WebBaseHandler newHandler(){
        return new GatewayHandler();
    }
}
