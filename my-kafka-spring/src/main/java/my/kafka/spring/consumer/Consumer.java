package my.kafka.spring.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Consumer {
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);

    // the KafkaListener annotation is with spring-kafka, not spring-cloud-stream
    @KafkaListener(topics = "test001", groupId = "testConsumer009")
    // this listener uses the spring default configuration: consumerFactory and kafkaListenerContainerFactory
    // and settings are from application.yml "spring.kafka.consumer.bootstrap-servers..."
    public void consume(String message) throws IOException {
        logger.info(String.format("#### -> Consumed message -> %s", message));
    }

    @KafkaListener(topics = "bank-transactions", groupId = "testConsumer009",
            containerFactory = "bankKafkaListenerContainerFactory")
    // this listener uses the custom configuration in KafkaConsumerConfig: bankKafkaListenerContainerFactory
    public void consume(ConsumerRecord<String, Double> record) throws IOException {
        logger.info("key: {}, message: {}", record.key(), record.value());
    }
}
