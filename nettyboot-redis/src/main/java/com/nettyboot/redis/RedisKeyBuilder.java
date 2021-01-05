package com.nettyboot.redis;

public class RedisKeyBuilder {
    public final String node;

    public final String prefix;

    public final int expireTime;

    public RedisKeyBuilder(String node, String prefix, int expireTime) {
        this.node = node;
        this.prefix = prefix;
        this.expireTime = expireTime;
    }

    public RedisKey build() {
        return new RedisKey(this);
    }

}

