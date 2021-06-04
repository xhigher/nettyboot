package com.nettyboot.chromedirver.pool;

import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.SessionId;

import java.util.Properties;

/**
 * ChromeDriver池的包装对象
 *
 * @author : lmr2015
 * @version : v1.0
 * @Description : // TODO
 * @createTime : 2021/6/3 11:16
 * @updateTime : 2021/6/3 11:16
 */
public class ChromeDriverPooledObject extends DefaultPooledObject<ChromeDriver> {

    /**
     * Creates a new instance that wraps the provided object so that the pool can
     * track the state of the pooled object.
     *
     * @param object The object to wrap
     */
    public ChromeDriverPooledObject(ChromeDriver object) {
        super(object);
    }

}
