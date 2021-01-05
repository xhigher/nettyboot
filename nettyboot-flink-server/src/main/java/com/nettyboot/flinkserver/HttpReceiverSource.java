package com.nettyboot.flinkserver;

import com.alibaba.fastjson.JSONObject;
import com.nettyboot.server.BaseServer;
import com.nettyboot.webserver.WebBaseHandler;
import com.nettyboot.webserver.WebBaseServer;
import io.netty.channel.ChannelHandlerContext;
import org.apache.flink.streaming.api.watermark.Watermark;

import java.util.Properties;

public class HttpReceiverSource<String> extends BaseSource {


    @Override
    protected BaseServer createServer(Properties properties) {
        return new SimpleHttpServer(properties);
    }

    @Override
    protected void init(Properties properties) {

    }

    @Override
    protected void release() {

    }

    class SimpleHttpServer extends WebBaseServer {

        private HttpSource httpSource;

        public SimpleHttpServer(Properties properties) {
            super(properties);
            httpSource = new HttpSource();
        }

        @Override
        protected void init(Properties properties) {
            //
        }

        @Override
        protected WebBaseHandler newHandler() {
            return new SimpleHttpHandler(httpSource);
        }
    }

    class SimpleHttpHandler extends WebBaseHandler {

        private final HttpSource httpSource;

        public SimpleHttpHandler(HttpSource httpSource){
            this.httpSource = httpSource;
        }

        @Override
        protected boolean checkRequest(ChannelHandlerContext context) {
            return true;
        }

        @Override
        protected void executeLogic(ChannelHandlerContext context) {

            this.httpSource.collect(this.requestInfo.getParameters());
        }
    }

    class HttpSource implements SourceContext<JSONObject>{

        @Override
        public void collect(JSONObject o) {

        }

        @Override
        public void collectWithTimestamp(JSONObject o, long l) {

        }

        @Override
        public void emitWatermark(Watermark watermark) {

        }

        @Override
        public void markAsTemporarilyIdle() {

        }

        @Override
        public Object getCheckpointLock() {
            return null;
        }

        @Override
        public void close() {

        }
    }

}
