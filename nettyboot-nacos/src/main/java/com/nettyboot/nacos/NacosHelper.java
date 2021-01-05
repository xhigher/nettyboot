package com.nettyboot.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Properties;

public class NacosHelper {


    private final String serverHost;
    private final int serverPort;

    private ConfigService configService;
    private NamingService namingService;

    public NacosHelper(Properties properties){
        serverHost = properties.getProperty("nacos.host").trim();
        serverPort = Integer.parseInt(properties.getProperty("nacos.port").trim());

    }

    public void initConfigService(){
        try{
            Properties properties = new Properties();
            properties.put("serverAddr", String.format("%s:%d", serverHost, serverPort));
            configService = NacosFactory.createConfigService(properties);
        }catch (Exception e){

        }
    }

    public void initNamingService(){
        try{
            namingService = NacosFactory.createNamingService(String.format("%s:%d", serverHost, serverPort));
        }catch (Exception e){
        }
    }

    public String getConfig(String dataId, String group){
        try{
            if(configService == null){
                initConfigService();
            }
            if(configService != null){
                return configService.getConfig(dataId, group, 5000);
            }
        }catch (Exception e){

        }
        return null;
    }

    public void setConfig(String dataId, String group, String data){
        try{
            if(configService == null){
                initConfigService();
            }
            if(configService != null){
                configService.publishConfig(dataId, group, data);
            }
        }catch (Exception e){

        }
    }

    public void removeConfig(String dataId, String group){
        try{
            if(configService == null){
                initConfigService();
            }
            if(configService != null){
                configService.removeConfig(dataId, group);
            }
        }catch (Exception e){

        }
    }

    public void addConfigListener(String dataId, String group, ConfigListener listener){
        try{
            if(configService == null){
                initConfigService();
            }
            if(configService != null) {
                configService.addListener(dataId, group, listener);
            }
        }catch (Exception e){

        }
    }

    public void registerService(String name, String host, int port){
        try{
            if(namingService == null){
                initNamingService();
            }
            if(namingService != null){
                namingService.registerInstance(name, host, port);
            }
        }catch (Exception e){

        }
    }

    public void deregisterService(String name, String host, int port){
        try{
            if(namingService == null){
                initNamingService();
            }
            if(namingService != null){
                namingService.deregisterInstance(name, host, port);
            }
        }catch (Exception e){

        }
    }

    public List<Instance> getAllServices(String name){
        try{
            if(namingService == null){
                initNamingService();
            }
            if(namingService != null){
                namingService.getAllInstances(name);
            }
        }catch (Exception e){

        }
        return null;
    }

    public void subscribe(String name, ServiceListener listener){
        try{
            if(namingService == null){
                initNamingService();
            }
            if(namingService != null){
                namingService.subscribe(name, listener);
            }
        }catch (Exception e){

        }
    }




}
