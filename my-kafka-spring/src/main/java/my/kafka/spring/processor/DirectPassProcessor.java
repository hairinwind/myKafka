package my.kafka.spring.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DirectPassProcessor {

    private static final String BANK_TOPIC = "bank-transactions";

    @Autowired
    private KafkaTemplate<String, Double> bankKafkaTemplate;

    @KafkaListener(topics = "bank-transactions-raw", groupId = "testConsumer007",
            containerFactory = "bankDeserializationKafkaListenerContainerFactory")
    public void consumeWithErrors(ConsumerRecord<String, Double> record) throws IOException {
        bankKafkaTemplate.send(BANK_TOPIC, record.key(), record.value());
    }

}
