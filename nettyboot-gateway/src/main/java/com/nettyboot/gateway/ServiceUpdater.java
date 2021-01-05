package com.nettyboot.gateway;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.rpcclient.SimpleClient;
import com.nettyboot.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceUpdater {
    private static final Logger logger = LoggerFactory.getLogger(ServiceUpdater.class);

    private static final ConcurrentHashMap<String, SimpleClient> serviceNodeClients = new ConcurrentHashMap<>();
    private static final  ConcurrentHashMap<String, String> nodeMD5Map = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, String> tempNodeMD5Map;
    private final ConcurrentHashMap<String, JSONObject> tempNodeConfigMap;

    private ServiceUpdater(){
        tempNodeMD5Map = new ConcurrentHashMap<>();
        tempNodeConfigMap = new ConcurrentHashMap<>();
    }

    public static ServiceUpdater build(){
        return new ServiceUpdater();
    }

    private void clearTempConfig(){
        tempNodeConfigMap.clear();
        tempNodeMD5Map.clear();
    }

    private void saveTempConfig(String nodeHostPort, JSONObject nodeConfig, String configMD5){
        tempNodeConfigMap.put(nodeHostPort, nodeConfig);
        tempNodeMD5Map.put(nodeHostPort, configMD5);
    }

    private boolean hasNewNodeConfig(String nodeHostPort){
        return tempNodeConfigMap.containsKey(nodeHostPort);
    }

    private boolean isDiffConfig(String nodeHostPort){
        return !tempNodeMD5Map.containsKey(nodeHostPort) ||
                !tempNodeMD5Map.containsKey(nodeHostPort) ||
                !nodeMD5Map.get(nodeHostPort).equals(tempNodeMD5Map.get(nodeHostPort));
    }

    private void connectServiceNode(String nodeHostPort) {
        String[] host_port = nodeHostPort.split(":");
        serviceNodeClients.put(nodeHostPort, ClientManager.getClient(host_port[0], Integer.parseInt(host_port[1])));
        updateServiceNode(nodeHostPort);
    }

    private void updateServiceNode(String nodeHostPort) {
        if(tempNodeConfigMap.containsKey(nodeHostPort)){
            ServiceManager.updateLogics(tempNodeConfigMap.get(nodeHostPort), serviceNodeClients.get(nodeHostPort));
            nodeMD5Map.put(nodeHostPort, tempNodeMD5Map.get(nodeHostPort));
        }
    }

    protected void updateAllNodeConfigs(List<String> allNodeConfigs) {
        if (allNodeConfigs != null) {
            if (allNodeConfigs.size() > 0) {
                //update local serverNodes cache
                clearTempConfig();

                String nodeHostPort = null;
                String nodeConfigsString = null;
                String nodeConfigsMD5 = null;
                JSONObject nodeConfigData = null;
                List<String> nodeHostPortList = new ArrayList<>();
                for (int i = 0; i < allNodeConfigs.size(); ++i) {
                    nodeConfigsString = allNodeConfigs.get(i);
                    nodeConfigsMD5 = StringUtil.md5(nodeConfigsString);
                    nodeConfigData = JSONObject.parseObject(nodeConfigsString);

                    nodeHostPort = nodeConfigData.getString("host") + ":" + nodeConfigData.getIntValue("port");
                    nodeHostPortList.add(nodeHostPort);
                    saveTempConfig(nodeHostPort, nodeConfigData, nodeConfigsMD5);
                }
                // Add new server node
                for (int i = 0; i < nodeHostPortList.size(); ++i) {
                    nodeHostPort = nodeHostPortList.get(i);
                    if (!serviceNodeClients.containsKey(nodeHostPort)) {
                        logger.info("Connect to service node " + nodeHostPort);
                        connectServiceNode(nodeHostPort);
                    } else {
                        SimpleClient client = serviceNodeClients.get(nodeHostPort);
                        if (client.isClosed()) {
                            connectServiceNode(nodeHostPort);
                        }
                        if (isDiffConfig(nodeHostPort)) {
                            updateServiceNode(nodeHostPort);
                        }
                    }
                }

                // Close and remove invalid server nodes
                for (String nodeHostPort2 : serviceNodeClients.keySet()) {
                    if (!hasNewNodeConfig(nodeHostPort2)) {
                        logger.warn("Remove invalid service node " + nodeHostPort2);
                        SimpleClient client = serviceNodeClients.get(nodeHostPort2);
                        if (client != null) {
                            client.shutdown();
                        }
                        serviceNodeClients.remove(nodeHostPort2);
                    }
                }

                clearTempConfig();
            } else {
                logger.error("No available service nodes. All service nodes are down !!!");
                for (SimpleClient client : serviceNodeClients.values()) {
                    if (client != null) {
                        client.shutdown();
                    }
                }
                serviceNodeClients.clear();
            }
        }
    }

}
