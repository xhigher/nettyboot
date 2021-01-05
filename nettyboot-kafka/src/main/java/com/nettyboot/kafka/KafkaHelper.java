package com.nettyboot.kafka;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Pattern;

public class KafkaHelper {

    private static final Logger logger = LoggerFactory.getLogger(KafkaHelper.class);

    private static String bootstrapServers;
    private static String groupId;
    //private static String keyDeserializer;
    //private static String valueDeserializer;

    private static KafkaConsumer<String, String> consumer;

    private static KafkaProducer<String, String> producer;

    public static void initProducer(Properties properties){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("kafka.producer.servers", ""));//kafka地址，多个地址用逗号分割
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        producer = new KafkaProducer<String, String>(props);
    }

    public static void initConsumer(Properties properties){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("kafka.consumer.servers", ""));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getProperty("kafka.consumer.group_id", ""));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        consumer = new KafkaConsumer<String, String>(props);
    }

    public static void send(String topic, String data){
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, data);
        producer.send(record);
    }

    public static void subscribe(Pattern pattern, RecordHandler handler){
        consumer.subscribe(pattern);
        try {
            Duration duration = Duration.ofSeconds(100);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(duration);
                for (ConsumerRecord<String, String> record : records) {
                    logger.debug("topic=%s, partition=%s, offset=%d, customer=%s, country=%s",
                            record.topic(), record.partition(), record.offset(),
                            record.key(), record.value());

                    handler.process(record.topic(), record.key(), record.value());

                }

                consumer.commitSync();
            }
        } finally {
            consumer.close();
        }
    }


    public static interface RecordHandler<K, V> {
        public void process(String topic, K key, V value);
    }

    public static void release(){
        consumer.wakeup();
    }

}
