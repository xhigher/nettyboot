package com.nettyboot.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolAbstract;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/*
 * @copyright (c) xhigher 2015 
 * @author xhigher    2015-3-26 
 */
public class XRedis {

	private static final Logger logger = LoggerFactory.getLogger(XRedis.class);

	private static final String FLAG_VALUE = "1";

	private static final String CHANNEL_PREFIX = "channel_";
	
	private final static Map<String,JedisPoolAbstract> redisPoolNodeList = new HashMap<>();

	private static JedisCluster redisCluster = null;

	private static boolean initStarted = false;
	private static boolean initOK = false;

	public static void init(Properties properties) {
		if(properties.containsKey("redis.status") && 1==Integer.parseInt(properties.getProperty("redis.status").trim())) {
			if(initStarted || initOK){
				return;
			}
			initStarted = true;
			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(Integer.parseInt(properties.getProperty("redis.pool.maxActive").trim()));
			poolConfig.setMaxIdle(Integer.parseInt(properties.getProperty("redis.pool.maxIdle").trim()));
			poolConfig.setMaxWaitMillis(Long.parseLong(properties.getProperty("redis.pool.maxWait").trim()));
			poolConfig.setTestOnBorrow(Boolean.parseBoolean(properties.getProperty("redis.pool.testOnBorrow").trim()));
			poolConfig.setTestOnReturn(Boolean.parseBoolean(properties.getProperty("redis.pool.testOnReturn").trim()));
			poolConfig.setBlockWhenExhausted(false);

			String name = null;
			String host = null;
			Integer port  = null;
			String pass = null;
			int db = 0;

			if(properties.containsKey("redis.cluster") && 1==Integer.parseInt(properties.getProperty("redis.cluster").trim())) {
				Set<HostAndPort> nodes = new LinkedHashSet<HostAndPort>();
				int nodeSize = Integer.parseInt(properties.getProperty("redis.node.size").trim());
				for (int id = 1; id <= nodeSize; id++) {
					nodes.add(new HostAndPort(properties.getProperty("redis.node"+id+".host").trim(),
							Integer.valueOf(properties.getProperty("redis.node"+id+".port").trim())));

				}
				redisCluster = new JedisCluster(nodes, poolConfig);
			}else{
				int nodeSize = Integer.parseInt(properties.getProperty("redis.node.size").trim());
				for (int id = 1; id <= nodeSize; id++) {
					name = properties.getProperty("redis.node"+id+".name").trim();

					if(properties.containsKey("redis.node"+id+".sentinel.status") && 1==Integer.parseInt(properties.getProperty("redis.node"+id+".sentinel.status").trim())){
						JedisSentinelPool jedisSentinelPool = getJedisSentinelPool(properties, id, poolConfig);
						redisPoolNodeList.put(name, jedisSentinelPool);
					}else{
						host = properties.getProperty("redis.node"+id+".host").trim();
						port = Integer.valueOf(properties.getProperty("redis.node"+id+".port").trim());
						pass = properties.getProperty("redis.node"+id+".pass").trim();
						db = 0;
						if(properties.containsKey("redis.node"+id+".db")) {
							db = Integer.parseInt(properties.getProperty("redis.node"+id+".db").trim());
						}
						redisPoolNodeList.put(name, new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, pass, db));
					}
				}
			}

			initOK = true;
			logger.info("redisPoolNodeList: names="+redisPoolNodeList.keySet().toString());
		}
	}

	private static JedisSentinelPool getJedisSentinelPool(Properties properties, int noteIndex, JedisPoolConfig poolConfig){
		String name = properties.getProperty("redis.node" + noteIndex + ".sentinel.name").trim();
		String sentinelsHostAndPorts = properties.getProperty("redis.node" + noteIndex + ".sentinel.hosts").trim();
		Set<String> sentinels = new HashSet<>(Arrays.asList(sentinelsHostAndPorts.split(",")));
		String pass = properties.getProperty("redis.node"+noteIndex+".pass").trim();
		int db = 0;
		if(properties.containsKey("redis.node"+noteIndex+".db")) {
			db = Integer.parseInt(properties.getProperty("redis.node"+noteIndex+".db").trim());
		}

		return new JedisSentinelPool(name, sentinels, poolConfig, Protocol.DEFAULT_TIMEOUT, pass, db);
	}

	private synchronized static Jedis getResource(String name) {
		Jedis jedis = null;
		JedisPoolAbstract jedisPool = redisPoolNodeList.get(name);
		if (jedisPool != null) {
			try{
				jedis = jedisPool.getResource();
			}catch(Exception e){
				logger.error("getResource.Exception:", e);
			}
		}
		return jedis;
	}

	public static void close() {
		for (JedisPoolAbstract jedisPool : redisPoolNodeList.values()) {
			jedisPool.close();
		}
		if(redisCluster != null){
			redisCluster.close();
		}
	}
	
	public static Long ttl(RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.ttl(key.name());
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.ttl(key.name());
			} catch (Exception e) {
				logger.error("ttl.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}
	
	public static Long expire(RedisKey key) {
	    if(key != null) {
			if(redisCluster != null){
				return redisCluster.expire(key.name(), key.builder.expireTime);
			}
	    	Jedis jedis = null;
	    	try{
	    		jedis = getResource(key.builder.node);
	    		if(jedis == null) {
	    			logger.error("getResource.null: node="+key.builder.node);
	    			return null;
	    		}
	    		return jedis.expire(key.name(), key.builder.expireTime);
			}catch(Exception e){
				logger.error("expire.Exception:", e);
			}finally {
				if (jedis != null) {
					jedis.close();
				}
			}
	    }
	    return 0L;
	}

	public static Long expireAt(RedisKey key, long unixTime) {
		if(key != null) {
			if(redisCluster != null){
				return redisCluster.expireAt(key.name(), unixTime);
			}
			Jedis jedis = null;
			try{
				jedis = getResource(key.builder.node);
				if(jedis == null) {
					logger.error("getResource.null: node="+key.builder.node);
					return null;
				}
				return jedis.expireAt(key.name(), unixTime);
			}catch(Exception e){
				logger.error("expireAt.Exception:", e);
			}finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	
	public static void set(RedisKey key, String value) {
	    if(key != null) {
			String keyName = key.name();
			if(redisCluster != null){
				redisCluster.set(keyName, value);
				if(key.builder.expireTime > 0) {
					redisCluster.expire(keyName, key.builder.expireTime);
				}
			}
	    	Jedis jedis = null;
	    	try{
	    		jedis = getResource(key.builder.node);
	    		if(jedis == null) {
	    			logger.error("getResource.null: node="+key.builder.node);
	    			return;
	    		}
				jedis.set(keyName, value);
	    		if(key.builder.expireTime > 0) {
	    			jedis.expire(keyName, key.builder.expireTime);
	    		}
			}catch(Exception e){
				logger.error("set.Exception:", e);
			}finally {
				if (jedis != null) {
					jedis.close();
				}
			}
	    }
	}
	
	public static String get(RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.get(key.name());
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.get(key.name());
			} catch (Exception e) {
				logger.error("get.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static List<String> mget(final RedisKey... keys) {
		if (keys != null && keys.length > 0) {
			String[] keyList = new String[keys.length];
			for(int i=0; i < keys.length; i++) {
				keyList[i] = keys[i].name();
			}
			if(redisCluster != null){
				return redisCluster.mget(keyList);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(keys[0].builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + keys[0].builder.node);
					return null;
				}
				return jedis.mget(keyList);
			} catch (Exception e) {
				logger.error("mget.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
			return null;
		}
		return Collections.emptyList();
	}

	public static boolean mset(final Map<RedisKey, String> data) {
		if (data != null && data.size() > 0) {
			String keyName = null;
			List<String> keyValues = null;
			if(redisCluster != null){
				int expireTime = 0;
				keyValues = new ArrayList<String>();
				Map<String, Integer> expireTimes = new HashMap<String, Integer>();
				for(RedisKey key : data.keySet()) {
					keyName = key.name();
					keyValues.add(keyName);
					keyValues.add(data.get(key));
				}
				redisCluster.mset(keyValues.toArray(new String[0]));
				for (String key : expireTimes.keySet()) {
					expireTime = expireTimes.get(key);
					if(expireTime > 0) {
						redisCluster.expire(key, expireTime);
					}
				}
				return true;
			}
			Map<String, List<String>> keyGroup = new HashMap<String, List<String>>();
			Map<String, Map<String, Integer>> keyExpires = new HashMap<String, Map<String, Integer>>();

			for(RedisKey key : data.keySet()) {
				if(!keyGroup.containsKey(key.builder.node)) {
					keyGroup.put(key.builder.node, new ArrayList<String>());
				}
				keyName = key.name();
				keyValues = keyGroup.get(key.builder.node);
				keyValues.add(keyName);
				keyValues.add(data.get(key));
				
				if(!keyExpires.containsKey(key.builder.node)) {
					keyExpires.put(key.builder.node, new HashMap<String, Integer>());
				}
				keyExpires.get(key.builder.node).put(keyName, key.builder.expireTime);
			}
			for(String node : keyGroup.keySet()) {
				if(!nodeMset(node, keyExpires.get(node), keyGroup.get(node).toArray(new String[0]))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	private static boolean nodeMset(String node, Map<String, Integer> expireTimes, String... keysvalues) {
		Jedis jedis = null;
		try {
			jedis = getResource(node);
			if (jedis == null) {
				logger.error("getResource.null: node=" + node);
				return false;
			}
			jedis.mset(keysvalues);
			int expireTime = 0;
			for (String key : expireTimes.keySet()) {
				expireTime = expireTimes.get(key);
				if(expireTime > 0) {
					jedis.expire(key, expireTime);
				}
			}
			return true;
		} catch (Exception e) {
			logger.error("nodeDel.Exception:", e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return false;
	}

	public static Boolean setnx(final RedisKey key, String value) {
		if (key != null) {
			if(redisCluster != null){
				if(redisCluster.setnx(key.name(), value) == 1){
					redisCluster.expire(key.name(), key.builder.expireTime);
					return true;
				}
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				if(jedis.setnx(key.name(), value) == 1){
					jedis.expire(key.name(), key.builder.expireTime);
					return true;
				}
			} catch (Exception e) {
				logger.error("setnx.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return false;
	}

	public static Long del(final RedisKey... keys) {
		if (keys != null && keys.length > 0) {
			if(redisCluster != null){
				String[] keyNames = new String[keys.length];
				for(int i=0; i<keys.length; i++) {
					keyNames[i] = keys[i].name();
				}
				return redisCluster.del(keyNames);
			}
			Map<String, List<String>> keyGroup = new HashMap<String, List<String>>();
			RedisKey key = null;
			for(int i=0; i<keys.length; i++) {
				key = keys[i];
				if(!keyGroup.containsKey(key.builder.node)) {
					keyGroup.put(key.builder.node, new ArrayList<String>());
				}
				keyGroup.get(key.builder.node).add(key.name());
			}
			for(String node : keyGroup.keySet()) {
				nodeDel(node, keyGroup.get(node).toArray(new String[0]));
			}
		}
		return 0L;
	}
	
	private static Long nodeDel(String node, String... keys) {
		Jedis jedis = null;
		try {
			jedis = getResource(node);
			if (jedis == null) {
				logger.error("getResource.null: node=" + node);
				return null;
			}
			return jedis.del(keys);
		} catch (Exception e) {
			logger.error("nodeDel.Exception:", e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return 0L;
	}


	
	public static Long del(RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.del(key.name());
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.del(key.name());
			} catch (Exception e) {
				logger.error("del.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static Long hdel(final RedisKey key, final String... fields) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.hdel(key.name(), fields);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.hdel(key.name(), fields);
			} catch (Exception e) {
				logger.error("hdel.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}
	
	public static Long incr(final RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.incr(key.name());
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.incr(keyName);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("incr.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static Long incrBy(final RedisKey key, long value) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.incrBy(key.name(), value);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.incrBy(keyName, value);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("incr.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}
	
	public static Boolean exists(RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.exists(key.name());
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.exists(key.name());
			} catch (Exception e) {
				logger.error("exists.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return false;
	}

	public static boolean hexists(RedisKey key, final String field) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.hexists(key.name(), field);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return false;
				}
				return jedis.hexists(key.name(), field);
			} catch (Exception e) {
				logger.error("hexists.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return false;
	}
	
	public static String hget(RedisKey key, final String field) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.hget(key.name(), field);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.hget(key.name(), field);
			} catch (Exception e) {
				logger.error("hget.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static Map<String, String> hgetAll(RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.hgetAll(key.name());
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.hgetAll(key.name());
			} catch (Exception e) {
				logger.error("hget.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static Long hset(final RedisKey key, final String field, final String value) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.hset(key.name(), field, value);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.hset(keyName, field, value);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("hset.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}
	
	public static String hmset(RedisKey key, final Map<String, String> hash) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.hmset(key.name(), hash);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				String result = jedis.hmset(keyName, hash);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("hmset.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}
	
	public static Map<String, String> hmget(RedisKey key, final String... fields) {
		if (key != null) {
			Map<String, String> result = new HashMap<>();
			if (fields.length == 0){
				return result;
			}
			if(redisCluster != null){
				List<String> values = redisCluster.hmget(key.name(), fields);
				for (int i = 0; i < fields.length; i++) {
					result.put(fields[i], values.get(i));
				}
				return result;
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				List<String> values = jedis.hmget(key.name(), fields);
				for (int i = 0; i < fields.length; i++) {
					result.put(fields[i], values.get(i));
				}
				return result;
			} catch (Exception e) {
				logger.error("hmget.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}
	
	public static Long sadd(RedisKey key, final String... members) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.sadd(key.name(), members);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.sadd(keyName, members);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("sadd.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static Set<String> smembers(final RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.smembers(key.name());
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.smembers(key.name());
			} catch (Exception e) {
				logger.error("smembers.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}


	public static Long srem(final RedisKey key, final String... members) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.srem(key.name(), members);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.srem(key.name(), members);
			} catch (Exception e) {
				logger.error("srem.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static Long lpush(RedisKey key, final String... elements) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.lpush(key.name(), elements);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.lpush(keyName, elements);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("sadd.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static Long rpush(RedisKey key, final String... elements) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.rpush(key.name(), elements);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				String keyName = key.name();
				Long result = jedis.rpush(keyName, elements);
				if(key.builder.expireTime > 0) {
					jedis.expire(keyName, key.builder.expireTime);
				}
				return result;
			} catch (Exception e) {
				logger.error("sadd.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static List<String> lrange(final RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.lrange(key.name(), 0, -1);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.lrange(key.name(), 0, -1);
			} catch (Exception e) {
				logger.error("smembers.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static List<String> lrange(final RedisKey key, int start, int stop) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.lrange(key.name(), start, stop);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.lrange(key.name(), start, stop);
			} catch (Exception e) {
				logger.error("smembers.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static Long lrem(final RedisKey key, final String element) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.lrem(key.name(), 0, element);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.lrem(key.name(), 0, element);
			} catch (Exception e) {
				logger.error("srem.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static String lpop(final RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.lpop(key.name());
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.lpop(key.name());
			} catch (Exception e) {
				logger.error("srem.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static String rpop(final RedisKey key) {
		if (key != null) {
			if(redisCluster != null){
				return redisCluster.rpop(key.name());
			}
			Jedis jedis = null;
			try {
				jedis = getResource(key.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + key.builder.node);
					return null;
				}
				return jedis.rpop(key.name());
			} catch (Exception e) {
				logger.error("srem.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return null;
	}

	public static Long publish(final RedisKey channel, String message) {
		if (channel != null) {
			if(redisCluster != null){
				return redisCluster.publish(channel.name(), message);
			}
			Jedis jedis = null;
			try {
				jedis = getResource(channel.builder.node);
				if (jedis == null) {
					logger.error("getResource.null: node=" + channel.builder.node);
					return null;
				}
				return jedis.publish(channel.name(), message);
			} catch (Exception e) {
				logger.error("publish.Exception:", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return 0L;
	}

	public static void subscribe(final JedisPubSub jedisPubSub, List<RedisKey> channels) {
		if (jedisPubSub != null && channels != null && channels.size() > 0) {
			String node = null;
			RedisKey channel = null;
			String[] keys = new String[channels.size()];
			for(int i=0; i<channels.size(); i++) {
				channel = channels.get(i);
				if(node == null) {
					node = channel.builder.node;
				}else if (node!=null && !node.equals(channel.builder.node)){
					logger.error("subscribe.node: different nodes");
					return;
				}
				keys[i] = channel.name();
			}
			if(redisCluster != null){
				redisCluster.subscribe(jedisPubSub, keys);
				return;
			}

			Jedis jedis = null;
			try {
				jedis = getResource(node);
				if (jedis == null) {
					logger.error("subscribe.getResource.null: node=" + node);
					return;
				}
				jedis.subscribe(jedisPubSub, keys);
			} catch (Exception e) {
				logger.error("subscribe.Exception", e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
	}

	public static Boolean setFlag(final RedisKey key){
		return XRedis.setnx(key, FLAG_VALUE);
	}

	public static RedisKey getChannelRedisKey(String node, String name) {
		RedisKey redisKey = null;
		if(name.startsWith(CHANNEL_PREFIX)) {
			RedisKeyBuilder builder = new RedisKeyBuilder(node, name, 0);
			redisKey = builder.build();
		}
		return redisKey;
	}
}
