package com.nettyboot.redis;

import com.nettyboot.config.TaskAnnotation;
import com.nettyboot.config.TaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SubscriberManager {

    private static Logger logger = LoggerFactory.getLogger(SubscriberManager.class);

    private static final SimplePubSub simplePubSub = new SimplePubSub();

    public static void initChannels(List<RedisKey> channels){

        XRedis.subscribe(simplePubSub, channels);
    }

    public static void submitTask(String channel, String message){

    }


    static class SimplePubSub extends JedisPubSub {

        @Override
        public void onMessage(String channel, String message) {
            SubscriberManager.submitTask(channel, message);
        }

        public void unsubscribe(RedisKey... channels) {
            String node = null;
            RedisKey channel = null;
            String[] keys = new String[channels.length];
            for(int i=0; i<channels.length; i++) {
                channel = channels[i];
                if(node == null) {
                    node = channel.builder.node;
                }else if (!node.equals(channel.builder.node)){
                    logger.error("subscribe.node: different nodes");
                    return;
                }
                keys[i] = channel.name();
            }
            this.unsubscribe(keys);
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            logger.info("onSubscribe: channel="+channel+", subscribedChannels="+subscribedChannels);
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            logger.info("onUnsubscribe: channel="+channel+", subscribedChannels="+subscribedChannels);
        }
    }
}
