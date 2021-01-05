package com.nettyboot.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class RabbitMQHelper {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQHelper.class);

    private static String host;
    private static int port;
    private static String queueName;
    private static String username;
    private static String password;
    private static int connectionCount = 1;

    private static ConnectionFactory connectionFactory = null;

    private static ChannelFactory channelFactory = null;
    private static ChannelPool channelPool = null;

    private static AtomicInteger connectionAtomic = new AtomicInteger();
    private static List<Connection> connectionList = null;

    public static void init(Properties properties){
        try{
            host = properties.getProperty("rabbitmq.host", "").trim();
            port = Integer.parseInt(properties.getProperty("rabbitmq.port", "5672").trim());
            queueName = properties.getProperty("rabbitmq.queue_name", "").trim();
            username = properties.getProperty("rabbitmq.username", "").trim();
            password = properties.getProperty("rabbitmq.password", "").trim();
            connectionCount = Integer.parseInt(properties.getProperty("rabbitmq.connection_count", "1").trim());
            if(connectionCount < 0){
                connectionCount = 1;
            }else if(connectionCount > 100){
                connectionCount = 100;
            }

            connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);

            connectionList = new ArrayList<>(connectionCount);
            for (int i=0; i<connectionCount; i++){
                connectionList.add(connectionFactory.newConnection());
            }

            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxTotal(4096);
            poolConfig.setMaxWaitMillis(1000);

            channelFactory = new ChannelFactory();
            channelPool = new ChannelPool(channelFactory, poolConfig);
        }catch (Exception e){
            logger.error("init.Exception", e);
        }
    }

    protected static Connection getConnection(){
        if(connectionList.size() == 1){
            return connectionList.get(0);
        }else {
            int index = connectionAtomic.getAndAdd(1) % connectionList.size();
            return connectionList.get(index);
        }
    }

    public static void exchangeDeclare(Channel channel, String exchangeName){
        try{
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC, false, true, null);
        }catch (Exception e){

        }
    }

    public static void queueDeclare(Channel channel, String queueName){
        try{
            channel.queueDeclare(queueName, false, false, true, null);
        }catch (Exception e){

        }
    }

    public static void queueBind(Channel channel, String queueName, String exchangeName, String routingKey){
        try{
            channel.queueBind(queueName, exchangeName, routingKey);
        }catch (Exception e){

        }
    }


    public static void publishMessage(String message){
        Channel channel = null;
        try{
            channel = channelPool.borrowObject();

        }catch (Exception e){

        }
    }

    public static void release(){
        try{
            for (int i=0; i<connectionList.size(); i++){
                connectionList.get(i).close();
            }
        }catch (Exception e){

        }
    }
}
