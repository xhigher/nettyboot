package com.nettyboot.starter;

import com.nettyboot.server.BaseServer;
import com.nettyboot.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public abstract class BaseStarter {

    protected static Logger logger = LoggerFactory.getLogger(BaseStarter.class);

    private static final String PROPERTIES_FILEPATH = "/application.properties";

    private BaseServer server = null;

    protected abstract BaseServer createServer(Properties properties);

    protected abstract void init(Properties properties);

    protected abstract void release();



    private void startServer(Properties properties){
        server = createServer(properties);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                server.stop();
                try{
                    release();
                }catch(Exception e){
                    logger.error("release.Exception:", e);
                }
            }
        });

        server.start();
    }

    public void run(){
        try{
            Properties properties = FileUtil.getProperties(PROPERTIES_FILEPATH);

            this.init(properties);

            this.startServer(properties);

        }catch(Exception e){
            logger.error("run.Exception:", e);
        }
    }

}
