package com.nettyboot.rabbitmq;


import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.Channel;

public class ChannelFactory extends BasePooledObjectFactory<Channel> {

    private static final Logger logger = LoggerFactory.getLogger(ChannelFactory.class);

    private Connection getConnection(){
        return RabbitMQHelper.getConnection();
    }

    @Override
    public Channel create() throws Exception {
        return getConnection().createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
    }
}
