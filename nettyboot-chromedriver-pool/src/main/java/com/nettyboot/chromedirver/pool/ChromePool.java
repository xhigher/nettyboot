package com.nettyboot.chromedirver.pool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Properties;

/**
 * ChromeDriver池
 *
 * @author : lmr2015
 * @version : v1.0
 * @Description : // TODO
 * @createTime : 2021/6/3 11:36
 * @updateTime : 2021/6/3 11:36
 */
public class ChromePool extends GenericObjectPool<ChromeDriver> {

    public ChromePool(GenericObjectPoolConfig<ChromeDriver> poolConfig) {
        this(new ChromeFactory(), poolConfig);
    }

    public ChromePool(PooledObjectFactory<ChromeDriver> factory, GenericObjectPoolConfig<ChromeDriver> poolConfig) {
        super(factory, poolConfig);
    }
}
