package com.nettyboot.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisKey {
        public final RedisKeyBuilder builder;

        private final List<Object> tags;
        private String name;

        public RedisKey(RedisKeyBuilder builder) {
            this.builder = builder;
            this.tags = new ArrayList<Object>();
            this.name = builder.prefix;
        }

        public RedisKey append(Object tag) {
            if(tag != null) {
                this.tags.add(tag);
            }
            return this;
        }

        public RedisKey reset() {
            this.tags.clear();
            return this;
        }

        public RedisKey append(Object[] tags) {
            if(tags != null) {
                for(int i=0; i<tags.length; i++) {
                    this.tags.add(tags[i]);
                }
            }
            return this;
        }

        public String name() {
            if(this.tags.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(builder.prefix);
                for(int i = 0; i < tags.size(); i++) {
                    sb.append(":");
                    sb.append(tags.get(i));
                }
                this.tags.clear();
                this.name = sb.toString();
            }
            return this.name;
        }

        public Long ttl() {
            return XRedis.ttl(this);
        }

        public Long expire() {
            return XRedis.expire(this);
        }

        public Long expireAt(long unixTime) {
            return XRedis.expireAt(this, unixTime);
        }

        public void set(String value) {
            XRedis.set(this, value);
        }

        public String get() {
            return XRedis.get(this);
        }

        public Boolean setnx(String value) {
            return XRedis.setnx(this, value);
        }

        public Boolean setFlag(){
            return XRedis.setFlag(this);
        }

        public Long del() {
            return XRedis.del(this);
        }

        public Long hdel(final String... fields) {
            return XRedis.hdel(this, fields);
        }

        public Long incr() {
            return XRedis.incr(this);
        }

        public Long incrBy(long value) {
            return XRedis.incrBy(this, value);
        }

        public Boolean exists() {
            return XRedis.exists(this);
        }

        public boolean hexists(String field) {
            return XRedis.hexists(this, field);
        }

        public String hget(String field) {
            return XRedis.hget(this, field);
        }

        public Map<String, String> hgetAll() {
            return XRedis.hgetAll(this);
        }

        public Long hset(final String field, final String value) {
            return XRedis.hset(this, field, value);
        }

        public String hmset(final Map<String, String> hash) {
            return XRedis.hmset(this, hash);
        }

        public Map<String, String> hmget(final String... fields) {
            return XRedis.hmget(this, fields);
        }

        public Long sadd(final String... members) {
            return XRedis.sadd(this, members);
        }

        public Set<String> smembers() {
            return XRedis.smembers(this);
        }

        public Long srem(final String... members) {
            return XRedis.srem(this, members);
        }

        public Long publish(String message) {
            return XRedis.publish(this, message);
        }
    }

