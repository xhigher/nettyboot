package com.nettyboot.test;

import com.nettyboot.chromedirver.ChromePoolHelper;
import com.nettyboot.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Properties;

/**
 * 一句话描述该类的功能
 *
 * @author : lmr2015
 * @version : v1.0
 * @Description : // TODO
 * @createTime : 2021/6/3 17:10
 * @updateTime : 2021/6/3 17:10
 */
public class ChromeTest {

    private static final String PROPERTIES_FILEPATH = "/test.properties";

    @Before
    public void init(){
        Properties properties = FileUtil.getProperties(PROPERTIES_FILEPATH);

        ChromePoolHelper.init(properties);
    }

    @After
    public void after(){
        ChromePoolHelper.close();
    }

    @Test
    public void test1(){
        for (int i = 0; i < 10; i++) {
            ChromeDriver driverFromPool = ChromePoolHelper.borrowDriver();

            driverFromPool.get("http://www.baidu.com");

            ChromePoolHelper.releaseDriver(driverFromPool);
        }

    }

    @Test
    public void test2() throws InterruptedException {
        new TestChromeTask("http://www.baidu.com").start();
        new TestChromeTask("http://www.qq.com").start();
        new TestChromeTask("http://www.360.com").start();
        new TestChromeTask("http://www.163.com").start();

        Thread.sleep(1000000);
    }

    static class TestChromeTask extends Thread{

        private final String url;

        public TestChromeTask(String url){
            this.url = url;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                ChromeDriver driverFromPool = ChromePoolHelper.borrowDriver();

                driverFromPool.get(url);

                try {
                    Thread.sleep(15_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ChromePoolHelper.releaseDriver(driverFromPool);
            }
        }
    }

    @Test
    public void test3(){
        for (int i = 0; i < 10; i++) {
            ChromeDriver driverFromPool = ChromePoolHelper.borrowDriver();

            driverFromPool.get("http://www.baidu.com");
            driverFromPool.quit();

            ChromePoolHelper.releaseDriver(driverFromPool);
        }

    }

    @Test
    public void test4(){
        for (int i = 0; i < 10; i++) {
            ChromeDriver driverFromPool = ChromePoolHelper.borrowDriver();

            driverFromPool.get("http://www.baidu.com");

            ChromePoolHelper.invalidateDriver(driverFromPool);
        }

    }

}
