package com.nettyboot.flinkserver;

import com.nettyboot.rpcmessage.SimpleMessage;
import com.nettyboot.rpcserver.HandlerContext;
import com.nettyboot.rpcserver.SimpleServer;
import com.nettyboot.server.BaseServer;
import io.netty.channel.ChannelHandlerContext;
import org.apache.flink.streaming.api.watermark.Watermark;

import java.util.Properties;

public class TcpReceiverSource<String> extends BaseSource {

    @Override
    protected BaseServer createServer(Properties properties) {
        return new SimpleServer(properties, new SimpleHandlerContext());
    }

    @Override
    protected void init(Properties properties) {

    }

    @Override
    protected void release() {

    }

    class SimpleHandlerContext extends HandlerContext{

        private final TcpSource tcpSource;

        public SimpleHandlerContext(){
            this.tcpSource = new TcpSource();
        }

        @Override
        public void handleMessage(ChannelHandlerContext context, SimpleMessage message) {
            this.tcpSource.collect(message);
        }

        @Override
        public boolean checkHeartBeat() {
            return false;
        }
    }

    class TcpSource implements SourceContext<SimpleMessage>{

        @Override
        public void collect(SimpleMessage o) {

        }

        @Override
        public void collectWithTimestamp(SimpleMessage o, long l) {

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
