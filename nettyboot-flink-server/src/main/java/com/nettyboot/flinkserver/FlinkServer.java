package com.nettyboot.flinkserver;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.operators.Keys;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.Properties;

public class FlinkServer {

    public static void start(ServerType type) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        if(type == ServerType.http){
            env.addSource(new HttpReceiverSource());
//            env.addSource(new HttpReceiverSource()).setParallelism(1).flatMap(new FlatMapFunction() {
//                @Override
//                public void flatMap(Object value, Collector out) throws Exception {
//
//                }
//            }).keyBy(Keys.ExpressionKeys).timeWindow(Time.seconds(30),Time.seconds(10));
        }else if(type == ServerType.tcp){
            env.addSource(new TcpReceiverSource());
        }

        env.execute();

    }
}
