package com.nettyboot.chromedirver;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ChromeHelper {

    private static final Logger logger = LoggerFactory.getLogger(ChromeHelper.class);

    private static String CHROMEDRIVER_PATH = "/usr/bin/chromedriver";

    private static boolean CHROMEDRIVER_HEADLESS = true;

    public static void init(Properties properties){
        if(properties.containsKey("webdriver.chrome.driver")) {
            CHROMEDRIVER_PATH = properties.getProperty("webdriver.chrome.driver").trim();
        }else {
            CHROMEDRIVER_PATH = "/usr/bin/chromedriver";
        }
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);

        if(properties.containsKey("webdriver.chrome.headless")){
            CHROMEDRIVER_HEADLESS = Integer.parseInt(properties.getProperty("webdriver.chrome.headless")) == 1;
        }
    }

    public static ChromeDriver getChromeDriver(){
        ChromeDriver driver = null;
        try {
            // 配置chromedriver 路径
            System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);

            ChromeOptions chromeOptions = new ChromeOptions();
            // 无窗口模式，linux模式下必须是无窗口模式
            if(CHROMEDRIVER_HEADLESS) {
                chromeOptions.addArguments("--headless");
            }
            // 禁用沙盒模式
            chromeOptions.addArguments("--no-sandbox");
            // 配置窗口启动大小
            chromeOptions.addArguments("--window-size=1920x1080");

            // 启动driver
            driver = new ChromeDriver(chromeOptions);
            return driver;
        } catch (Exception e) {
            logger.error("getChromeDriver error", e);
            if(driver != null){
                try {
                    driver.quit();
                }catch (Exception e2){
                }
            }
        }
        return null;
    }


    public static boolean openPage(ChromeDriver driver, String url){
        try {
            logger.info("openPage: {}", url);
            driver.get(url);
            // 检查页面是否是错误页面
            if(ChromeHelper.checkPageError(driver)){
                // 如果没有解决错误页面，则返回错误
                return ChromeHelper.handlePageError(driver, url);
            }
            return true;
        }catch (Exception e){
            logger.error("openPage ", e);
        }
        return false;
    }

    public static boolean checkPageError(ChromeDriver driver){
        try {
            if(driver.findElementById("main-message") != null
                    && driver.findElementById("error-information-popup-container") != null){
                logger.info("chromeDriver has error msg: {}", driver.findElementById("main-message").getText());
                return true;
            }
        }catch (NoSuchElementException e1){
            // 没有错误信息
            return false;
        }catch (Exception e){
            logger.error("checkPageNormal ", e);
        }
        return true;
    }

    public static boolean handlePageError(ChromeDriver driver, String url){
        int count = 3;
        while (count > 0){
            try {
                if(driver.findElementById("main-message") != null
                        && driver.findElementById("error-information-popup-container") != null){
                    logger.info("chromeDriver has error msg: {}", driver.findElementById("main-message").getText());

                    driver.get(url);
                    Thread.sleep(500);
                }
            }catch (NoSuchElementException e1){
                // 没有错误信息
                return true;
            }catch (Exception e){
                logger.error("checkPageNormal ", e);
            }finally {
                count--;
            }
        }
        return false;
    }

    public static boolean quitDriver(ChromeDriver driver){
        if(driver != null) {
            SessionId sessionId = driver.getSessionId();
            try {
                try {
                    driver.manage().deleteAllCookies();
                } catch (Exception e) {
                    logger.warn("ChromeHelper.quitDriver sessionId: {}, deleteAllCookies: {}", sessionId, e.getMessage());
                }
                try {
                    driver.close();
                } catch (Exception e) {
                    logger.warn("ChromeHelper.quitDriver sessionId: {}, close: {}", sessionId, e.getMessage());
                }
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.warn("ChromeHelper.quitDriver sessionId: {}, quit: {}", sessionId, e.getMessage());
                }
            } catch (Exception e) {
                logger.error("ChromeHelper.quitDriver sessionId: {}, Exception: {}", sessionId, e);
            }
        }
        return true;
    }

    public static void close(){

    }
}
