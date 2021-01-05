package com.nettyboot.rabbitmq;

import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class ChannelPool extends GenericObjectPool<Channel> {


    public ChannelPool(PooledObjectFactory<Channel> factory) {
        super(factory);
    }

    public ChannelPool(PooledObjectFactory<Channel> factory, GenericObjectPoolConfig<Channel> config) {
        super(factory, config);
    }

    public ChannelPool(PooledObjectFactory<Channel> factory, GenericObjectPoolConfig<Channel> config, AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
    }
}
