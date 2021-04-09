package my.kafka.partition.consumer;

import my.kafka.partition.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Service
public class Consumer {

    public void start() {
        try (final KafkaConsumer<String, Long> consumer = new KafkaConsumer<>(config())) {
            consumer.subscribe(Collections.singleton(Constants.PARTITION_TOPIC));

            while (true) {
                final ConsumerRecords<String, Long> consumerRecords = consumer.poll(Duration.ofMillis(500));
                for (final ConsumerRecord<String, Long> consumerRecord : consumerRecords) {
                    System.out.println(consumerRecord);
                }
            }
        }
    }

    protected Properties config() {
        Properties config = new Properties();
        config.put("bootstrap.servers", Constants.BOOTSTRAP_SERVERS);
        // different consumers of same group would take messages from different partition
        config.put("group.id", "testConsumer001");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return config;
    }
}
