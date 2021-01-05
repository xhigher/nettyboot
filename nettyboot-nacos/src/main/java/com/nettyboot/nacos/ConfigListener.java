package com.nettyboot.nacos;

import com.alibaba.nacos.api.config.listener.Listener;

import java.util.concurrent.Executor;

public abstract class ConfigListener implements Listener {

    private String dataId;
    private String group;

    public ConfigListener(){
    }

    public ConfigListener(String dataId, String group){
        this.dataId = dataId;
        this.group = group;
    }

    public abstract void onReceived(String data);

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void receiveConfigInfo(String data) {
        this.onReceived(data);
    }
}
