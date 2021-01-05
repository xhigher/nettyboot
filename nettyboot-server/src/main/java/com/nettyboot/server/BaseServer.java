package com.nettyboot.server;

import java.util.Properties;

public abstract class BaseServer {

    protected abstract void init(Properties properties);

    public abstract void start();

    public abstract void stop();

}
