package com.nettyboot.chromedirver;

import com.nettyboot.chromedirver.pool.ChromeFactory;
import com.nettyboot.chromedirver.pool.ChromePool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ChromePoolHelper {

    private static final Logger logger = LoggerFactory.getLogger(ChromePoolHelper.class);

    /**
     * ChromePool池子
     */
    private static ChromePool myChromePool;
    /**
     * 是否已经初始化，不可二次初始化
     */
    private static volatile boolean initPool = false;
    /**
     * 初始化的锁
     */
    private static final Object INIT_LOCK = new Object();
    /**
     * 最小闲置量
     */
    private static int POLL_MIN_IDLE = 1;
    /**
     * 最大闲置量
     */
    private static int POLL_MAX_IDLE = 1;
    /**
     * 池子最大容量
     */
    private static int POLL_MAX_TOTAL = 2;
    /**
     * 借用chrome对象时最大等待时间
     */
    private static long MAX_WAIT_MILLIS = -1;
    /**
     * 每个chrome对象最大使用次数
     */
    private static int MAX_USE_TIMES = 10;

    /**
     * 初始化参数
     * @param properties
     */
    public static void init(Properties properties){
        // 初始化ChromeHelper，用来生成chrome的工具类
        ChromeHelper.init(properties);

        if(properties.containsKey("webdriver.chrome.pool.minIdle")){
            POLL_MIN_IDLE = Integer.parseInt(properties.getProperty("webdriver.chrome.pool.minIdle"));
        }
        if(properties.containsKey("webdriver.chrome.pool.maxIdle")){
            POLL_MAX_IDLE = Integer.parseInt(properties.getProperty("webdriver.chrome.pool.maxIdle"));
        }
        if(properties.containsKey("webdriver.chrome.pool.maxTotal")){
            POLL_MAX_TOTAL = Integer.parseInt(properties.getProperty("webdriver.chrome.pool.maxTotal"));
        }
        if(properties.containsKey("webdriver.chrome.pool.maxWaitSeconds")){
            MAX_WAIT_MILLIS = Integer.parseInt(properties.getProperty("webdriver.chrome.pool.maxWaitSeconds")) * 1000L;
        }
        if(properties.containsKey("webdriver.chrome.pool.maxUseTimes")){
            MAX_USE_TIMES = Integer.parseInt(properties.getProperty("webdriver.chrome.pool.maxUseTimes"));
        }

        // 初始化chrome池
        initChromePool();

        // 异常挂断，触发close()，关闭所有浏览器对象
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                close();
            }
        });
    }

    /**
     * 初始化池子
     */
    public static void initChromePool(){
        if(!initPool){
            synchronized (INIT_LOCK){
                if(!initPool){
                    // 工厂类
                    ChromeFactory chromeFactory = new ChromeFactory(MAX_USE_TIMES);

                    //  配置
                    GenericObjectPoolConfig<ChromeDriver> poolConfig = new GenericObjectPoolConfig<ChromeDriver>();
                    poolConfig.setMinIdle(POLL_MIN_IDLE);
                    poolConfig.setMaxIdle(POLL_MAX_IDLE);
                    poolConfig.setMaxTotal(POLL_MAX_TOTAL);
                    poolConfig.setMaxWaitMillis(MAX_WAIT_MILLIS);
                    poolConfig.setTestOnReturn(true);

                    // 初始化
                    myChromePool = new ChromePool(chromeFactory, poolConfig);

                    initPool = true;
                }
            }
        }
    }

    /**
     * 租界driver
     * @return
     */
    public static ChromeDriver borrowDriver() {
        try {
            return myChromePool.borrowObject();
        } catch (Exception e) {
            logger.error("ChromePoolHelper.borrowDriver", e);
        }
        return null;
    }

    /**
     * 直接销毁driver
     * @param driver
     */
    public static void invalidateDriver(ChromeDriver driver){
        if(driver != null){
            try {
                myChromePool.invalidateObject(driver);
            } catch (Exception e) {
                logger.error("ChromePoolHelper.invalidateDriver.sessionId:" + driver.getSessionId(), e);
            }
        }
    }

    /**
     * 返还driver
     * @param driver
     */
    public static void releaseDriver(ChromeDriver driver){
        if(driver != null){
            try {
                if(driver.getSessionId() == null){
                    // sessionId 为null，则直接销毁
                    myChromePool.invalidateObject(driver);
                }else{
                    myChromePool.returnObject(driver);
                }
            }catch (Exception e){
                logger.error("ChromePoolHelper.releaseDriver.sessionId:" + driver.getSessionId(), e);
            }
        }
    }

    /**
     * 销毁池子内的对象
     */
    public static void close(){
        myChromePool.close();
    }
}
