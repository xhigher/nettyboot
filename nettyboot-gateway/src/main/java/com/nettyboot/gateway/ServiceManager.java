package com.nettyboot.gateway;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nettyboot.config.LogicConfig;
import com.nettyboot.rpcclient.SimpleClient;
import com.nettyboot.zookeeper.ZooKeeperHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    private static ZooKeeperHelper zookeeperHelper;

    private static final ConcurrentHashMap<String, AtomicInteger> logicRoundRobinMap = new ConcurrentHashMap<>();
    protected static final ConcurrentHashMap<String, LogicConfig> logicConfigs = new ConcurrentHashMap<String, LogicConfig>();
    protected static final ConcurrentHashMap<String, CopyOnWriteArrayList<SimpleClient>> logicServers = new ConcurrentHashMap<String, CopyOnWriteArrayList<SimpleClient>>();

    private static boolean clearClosedClientLocked = false;

    private static boolean initOK = false;

    protected static void init(Properties properties){
        if(!initOK){

            initZooKeeper(properties);

            initOK = true;
        }
    }

    private static void initZooKeeper(Properties properties){
        String product = properties.getProperty("service.product", "").trim();
        String business = properties.getProperty("service.business", "").trim();
        String servers = properties.getProperty("zookeeper.servers", "").trim();

        zookeeperHelper = new ZooKeeperHelper(product, business, servers);
        zookeeperHelper.initSubscriber(new ZooKeeperHelper.WatchNodeHandler() {
            @Override
            public void update(List<String> data) {
                ServiceUpdater.build().updateAllNodeConfigs(data);
            }
        });
    }

    private static void clearConfig(){
        logicConfigs.clear();
        logicServers.clear();
    }

    private static void clearClosedClient(){
        if(clearClosedClientLocked){
            return;
        }
        clearClosedClientLocked = true;
        SimpleClient client = null;
        CopyOnWriteArrayList<SimpleClient> activeClients = new CopyOnWriteArrayList<SimpleClient>();
        for (CopyOnWriteArrayList<SimpleClient> clients : logicServers.values()) {
            for(int i=0; i<clients.size(); i++){
                client = clients.get(i);
                if(!client.isClosed()){
                    activeClients.add(client);
                }
            }
            clients.clear();
            clients.addAll(activeClients);
            activeClients.clear();
        }
        clearClosedClientLocked = false;
    }

    public static LogicConfig getLogicConfig(String module, String action, int version){
        return logicConfigs.get(getLogicIdentifier(module, action, version));
    }

    public static String getLogicIdentifier(String module, String action, int version) {
        return module + "#" + action + "@" + version;
    }

    protected static String getLogicIdentifier(LogicConfig logicConfig) {
        if(logicConfig != null){
            return getLogicIdentifier(logicConfig.getModule(),
                    logicConfig.getAction(),
                    logicConfig.getVersion());
        }
        return null;
    }

    private static CopyOnWriteArrayList<SimpleClient> cloneClientList(String logicIdentifier){
        if(logicServers.containsKey(logicIdentifier)){
            return (CopyOnWriteArrayList<SimpleClient>)logicServers.get(logicIdentifier).clone();
        }
        return null;
    }

    private static int getClientRoundRobin(String logicIdentifier) {
    	if(logicRoundRobinMap.containsKey(logicIdentifier)) {
    		return logicRoundRobinMap.get(logicIdentifier).getAndAdd(1);
    	}
    	return 0;
    }

    public static SimpleClient getClient(String module, String action, int version) {
    	String logicIdentifier = getLogicIdentifier(module, action, version);
        if(logicServers.containsKey(logicIdentifier)){
            CopyOnWriteArrayList<SimpleClient> clients = logicServers.get(logicIdentifier);
            if (clients != null && clients.size() > 0) {
                int size = clients.size();
                int closedNum = 0;
                SimpleClient client = null;
                for (int index = 0, i = 0; i < size; i++) {
                    index = (getClientRoundRobin(logicIdentifier) + size) % size;
                    client = clients.get(index);
                    logger.info("client: address=" + client.getAddress());
                    if (client.isClosed()) {
                        closedNum++;
                    } else {
                        return client;
                    }
                }
                if (closedNum > 0) {
                    clearClosedClient();
                }
            }
        }
        return null;
    }

    protected static void updateLogics(JSONObject configData, SimpleClient client){
        if(configData != null){
            JSONArray logicList = configData.getJSONArray("logics");
            LogicConfig logicConfig = null;
            String logicIdentifier = null;
            for(int i=0,n=logicList.size(); i<n; i++){
                logicConfig = logicList.getObject(i, LogicConfig.class);
                logicIdentifier = ServiceManager.getLogicIdentifier(logicConfig);
                logicConfigs.put(logicIdentifier, logicConfig);

                if(!logicServers.containsKey(logicIdentifier)){
                    logicServers.put(logicIdentifier, new CopyOnWriteArrayList<SimpleClient>());
                }
                logicServers.get(logicIdentifier).add(client);
                logger.debug("updateLogics: logicIdentifier="+logicIdentifier+", client="+client);
            }
        }
    }

    public static void release() {
        clearConfig();
    }

}
