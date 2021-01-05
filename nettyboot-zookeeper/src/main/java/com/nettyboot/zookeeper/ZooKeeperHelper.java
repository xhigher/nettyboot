package com.nettyboot.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZooKeeperHelper {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperHelper.class);

    private static final int SESSION_TIMEOUT = 60000;

    private final String product;
    private final String business;
    private final String servers;
    private final CountDownLatch connectLatch;
    private int type = 0;

    private ZooKeeper zookeeper;

    public interface WatchNodeHandler {
        void update(List<String> data);
    }

    private WatchNodeHandler watchNodeHandler;

    public ZooKeeperHelper(String product, String business, String servers){
        this.product = product;
        this.business = business;
        this.servers = servers;
        this.connectLatch = new CountDownLatch(1);
    }

    public void initPublisher(String host, int port, String configData){
        if(this.type == 0) {
            this.type = 1;
            zookeeper = connectToZooKeeper();
            if (zookeeper != null) {
                checkNode(host, port, configData);
            }
        }
    }

    public void initSubscriber(WatchNodeHandler handler){
        if(handler == null){
            logger.error("initSubscriber: WatchNodeHandler is null!");
            return;
        }
        if(this.type == 0){
            this.type = 2;
            this.watchNodeHandler = handler;
            zookeeper = connectToZooKeeper();
            if (zookeeper != null) {
                watchNode();
            }
        }
    }

    private ZooKeeper connectToZooKeeper() {
        ZooKeeper zookeeper = null;
        try {
            zookeeper = new ZooKeeper(this.servers, SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        connectLatch.countDown();
                    }
                }
            });
            connectLatch.await();
        } catch (Exception e) {
            logger.error("connectToZooKeeper.Exception", e);
        }
        return zookeeper;
    }

    public void close(){
        if (zookeeper != null) {
            try{
                zookeeper.close();
            }catch (Exception e){
            }
        }
    }

    public void checkNode(String host, int port, String configData){
        try {
            StringBuilder nodePathSB = new StringBuilder();
            nodePathSB.append("/");
            nodePathSB.append(product);
            String nodePath = nodePathSB.toString();
            Stat stat = zookeeper.exists(nodePath, false);
            if (stat == null) {
                zookeeper.create(nodePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            nodePathSB.append("/");
            nodePathSB.append(business);
            nodePath = nodePathSB.toString();
            stat = zookeeper.exists(nodePath, false);
            if (stat == null) {
                zookeeper.create(nodePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            nodePathSB.append("/");
            nodePathSB.append(host);
            nodePathSB.append(":");
            nodePathSB.append(port);
            nodePath = nodePathSB.toString();
            byte[] configBytes = configData.getBytes();
            stat = zookeeper.exists(nodePath, false);
            if(stat == null) {
                zookeeper.create(nodePath, configBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            }else {
                zookeeper.setData(nodePath, configBytes, (int) (System.currentTimeMillis() / 1000 - 1500000000));
            }
        } catch (Exception e) {
            logger.error("checkRootNode.Exception", e);
        }
    }

    private void watchNode() {
        try {
            StringBuilder pathSB = new StringBuilder();
            pathSB.append("/");
            pathSB.append(product);
            String nodePath = pathSB.toString();
            Stat stat = zookeeper.exists(nodePath, false);
            if (stat == null) {
                zookeeper.create(nodePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            pathSB.append("/");
            pathSB.append(business);
            nodePath = pathSB.toString();
            stat = zookeeper.exists(nodePath, false);
            if (stat == null) {
                zookeeper.create(nodePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            List<String> nodeHostPortList = zookeeper.getChildren(nodePath, new Watcher() {
                public void process(WatchedEvent event) {
                    EventType type = event.getType();
                    if (type == EventType.NodeCreated || type == EventType.NodeCreated ||
                            type == EventType.NodeDataChanged || type == EventType.NodeChildrenChanged) {
                        watchNode();
                    }
                }
            });

            List<String> nodeConfigList = new ArrayList<>();
            for (String nodeHostPort : nodeHostPortList) {
                byte[] bytes = zookeeper.getData(nodePath + "/" + nodeHostPort, false, null);
                nodeConfigList.add(new String(bytes));
            }

            if(this.watchNodeHandler != null){
                this.watchNodeHandler.update(nodeConfigList);
            }

        } catch (Exception e) {
            logger.error("watchNode.Exception", e);
        }
    }
}
