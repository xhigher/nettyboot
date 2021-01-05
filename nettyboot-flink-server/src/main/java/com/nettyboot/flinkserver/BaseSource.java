package com.nettyboot.flinkserver;

import com.nettyboot.server.BaseServer;
import com.nettyboot.util.FileUtil;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public abstract class BaseSource<String> extends RichParallelSourceFunction {

    protected static Logger logger = LoggerFactory.getLogger(BaseSource.class);

    private static final java.lang.String PROPERTIES_FILEPATH = "/application.properties";

    private BaseServer server = null;

    protected abstract BaseServer createServer(Properties properties);

    protected abstract void init(Properties properties);

    protected abstract void release();

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        try{
            Properties properties = FileUtil.getProperties(PROPERTIES_FILEPATH);

            this.init(properties);

            this.startServer(properties);

        }catch(Exception e){
            logger.error("run.Exception:", e);
        }
    }

    private void startServer(Properties properties){
        logger.info("startServer");
        server = createServer(properties);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try{
                    release();
                }catch(Exception e){
                    logger.error("release.Exception:", e);
                }
                server.stop();
            }
        });

        server.start();
    }

    @Override
    public void cancel() {
        server.stop();
        this.release();
    }
}
