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

    @KafkaListener(topics = "bank-transactions", groupId = "testConsumer007",
            containerFactory = "bankKafkaListenerContainerFactory")
    // this method mimic error when consuming the messages
    // this listener uses the custom configuration: bankKafkaListenerContainerFactory
    // a dead letter topic is created if not exist, the name is bank-transactions.DLT
    public void consumeWithErrors(ConsumerRecord<String, Double> record) throws IOException {
        if (record.key().equals("100002")) { //mimic the error
            logger.info("mimic error, key: {}, message: {}", record.key(), record.value());
            throw new RuntimeException("test dead letter");
        }
        logger.info("consumed, key: {}, message: {}", record.key(), record.value());
    }

    // read the record from bank-transactions.DLT
    // these are the records which causes business exceptions
    @KafkaListener(topics = "bank-transactions.DLT", groupId = "dltGroup",
            containerFactory = "bankKafkaListenerContainerFactory")
    public void dltListen(ConsumerRecord<String, Double> record) {
        logger.info("record from bank-transactions.DLT: {}", record);
    }

    // read the record from bank-transactions-raw.DLT
    // these are the records which cannot be deserialized successfully
    @KafkaListener(topics = "bank-transactions-raw.DLT", groupId = "dltGroup-bytes",
            containerFactory = "bytesKafkaListenerContainerFactory")
    public void dltBytesListen(ConsumerRecord<String, byte[]> record) {
        logger.info("record from bank-transactions-raw.DLT: {}", record);
//        Headers headers = record.headers();
    }
}
