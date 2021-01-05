package com.nettyboot.flinkkafka;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class FlinkKafkaHelper {

    private static final Logger logger = LoggerFactory.getLogger(FlinkKafkaHelper.class);

    private static Properties kafkaProperties;

    public static void init(Properties properties){
        kafkaProperties = new Properties();
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("kafka.servers", ""));
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getProperty("kafka.group_id", ""));

    }

    public static void initProducer(String topic, SourceFunction<String> source){
        FlinkKafkaProducer producer = new FlinkKafkaProducer<String>(topic, new SimpleStringSchema(), kafkaProperties);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = env.addSource(source);
        stream.addSink(producer);

        try {
            env.execute();
        } catch (Exception e) {
            logger.error("initProducer.Exception", e);
        }
    }


    public static void initConsumer(String topic, MapFunction<String, String> task){
        FlinkKafkaConsumer consumer = new FlinkKafkaConsumer<String>(topic, new SimpleStringSchema(), kafkaProperties);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> source = env.addSource(consumer).setParallelism(1);

        source.map(task).print();

        try {
            env.execute();
        } catch (Exception e) {
            logger.error("initConsumer.Exception", e);
        }
    }
}
