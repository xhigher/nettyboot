package com.nettyboot.nacos;

import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.concurrent.Executor;

public abstract class ServiceListener implements EventListener {

    private String name;

    public ServiceListener(){
    }

    public ServiceListener(String name){
        this.name = name;
    }

    @Override
    public void onEvent(Event event){
        if(event instanceof NamingEvent){
            NamingEvent namingEvent = (NamingEvent) event;
            this.onReceived(namingEvent.getInstances(), namingEvent.getServiceName(),
                    namingEvent.getGroupName(),
                    namingEvent.getClusters());
        }
    }

    public abstract void onReceived(List<Instance> instances,  String name, String group, String clusters);

}
