package com.nettyboot.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPProxyManager {

	private static Logger logger = LoggerFactory.getLogger(IPProxyManager.class);
	
	private final static CopyOnWriteArrayList<String> proxyHostList = new CopyOnWriteArrayList<>();
    private final static long proxyIPsMinLength = 10;
    private static boolean initFlag = false;

    private final static Pattern urlPattern = Pattern.compile("^http[s]?://(.*?)/.*?$");

    private final static ConcurrentHashMap<String, Integer> websiteIPIndexMap = new ConcurrentHashMap<>(16);

    private final static ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();

    private static IPProxySource proxySource;

    public synchronized static void init(IPProxySource source){
        if(initFlag) {
            return;
        }
        proxySource = source;
        scheduledService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
            	reloadProxyHostList();
            }
        }, 5, 10, TimeUnit.SECONDS);
        initFlag = true;
    }

    public static void reloadProxyHostList(){
    	
        Map<String, String> cacheList = proxySource.getList();
        if(null != cacheList && cacheList.size() > proxyIPsMinLength){
            Set<String> newProxyHostList = cacheList.keySet();

            proxyHostList.addAllAbsent(newProxyHostList);
            proxyHostList.retainAll(newProxyHostList);
        }
        logger.info("reloadProxyIPList size: " + proxyHostList.size());
    }

    public synchronized static String getHostPort(String url){
        //logger.info("getIP: {}", url);
        Matcher matcher = urlPattern.matcher(url);
        if(! matcher.find()){
            return null;
        }
        if(proxyHostList.size() == 0){
            return null;
        }
        String domain = matcher.group(1);
        //logger.info("getIP domain: {}", domain);

        Integer index = websiteIPIndexMap.get(domain);
        if(index == null){
            index = 0;
            websiteIPIndexMap.put(domain, index);
        }
        String host = null;
        if(index < proxyHostList.size()){
        	host = proxyHostList.get(index);
            websiteIPIndexMap.put(domain, ++index);
        }else {
            index = 0;
            websiteIPIndexMap.put(domain, index);
            host = proxyHostList.get(index);
        }
        //logger.info("domain: " + domain + " host:" + host + " index: " + index);
        return host;
    }

    public interface IPProxySource{

        public Map<String, String> getList();
    }
}
