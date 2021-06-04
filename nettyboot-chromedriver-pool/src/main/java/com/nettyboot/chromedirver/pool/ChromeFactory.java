package com.nettyboot.chromedirver.pool;

import com.nettyboot.chromedirver.ChromeHelper;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.Augmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Set;

/**
 * ChromeDriver池的工厂类
 *
 * @author : lmr2015
 * @version : v1.0
 * @Description :
 * @createTime : 2021/6/3 11:16
 * @updateTime : 2021/6/3 11:16
 */
public class ChromeFactory extends BasePooledObjectFactory<ChromeDriver> {

    private static final Logger logger = LoggerFactory.getLogger(ChromeFactory.class);

    /**
     * 当前chrome最多使用次数
     */
    private int maxUseTimes = 10;

    public ChromeFactory(){ }

    public ChromeFactory(int maxUseTimes){
        this.maxUseTimes = maxUseTimes;
    }

    @Override
    public ChromeDriver create() throws Exception {
        return ChromeHelper.getChromeDriver();
    }

    @Override
    public PooledObject<ChromeDriver> wrap(ChromeDriver obj) {
        return new DefaultPooledObject<ChromeDriver>(obj);
    }

    @Override
    public void destroyObject(PooledObject<ChromeDriver> p, DestroyMode mode) throws Exception {
        if (p != null) {
            ChromeDriver driver = p.getObject();
            if(driver != null) {
                try {
                    ChromeHelper.quitDriver(driver);
                } catch (Exception ignore) { }
            }
        }
    }

    @Override
    public boolean validateObject(final PooledObject<ChromeDriver> p) {
        // 出借次数大于最大使用次数，或ChromeDriver无效，则判定为无效元素
        if(p.getBorrowedCount() >= this.maxUseTimes
                || p.getObject() == null
                || p.getObject().getSessionId() == null){
            return false;
        }
        return true;
    }


    @Override
    public void passivateObject(final PooledObject<ChromeDriver> p) {
        ChromeDriver driver = p.getObject();
        // 将浏览器置为空白的原始状态
        // 关闭多余标签，并把标签修改为空白页面
        String currentWindowHandle = driver.getWindowHandle();
        Set<String> windowHandles = driver.getWindowHandles();
        if (windowHandles.size() > 0) {
            for (String handle : windowHandles) {
                driver.switchTo().window(handle);

                try {
                    // 清空cookies
                    driver.manage().deleteAllCookies();
                } catch (Exception ignore) { }

                try {
                    // 清空LocalStorage
                    ((WebStorage) new Augmenter().augment(driver)).getLocalStorage().clear();
                } catch (Exception ignore) {
                }

                // 关闭多余的标签
                if (!currentWindowHandle.equals(handle)) {
                    driver.close();
                }
            }
            driver.switchTo().window(currentWindowHandle);
            // 页面切换为空页面
            driver.get("data:,");
        }
    }

}
