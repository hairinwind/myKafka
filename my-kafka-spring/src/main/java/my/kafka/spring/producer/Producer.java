package my.kafka.spring.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private static final String TOPIC = "test001";
    private static final String BANK_TOPIC = "bank-transactions";

    @Autowired
    // this is the default kafkaTemplate
    // key - value is String - String
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        logger.info(String.format("#### -> Producing message -> %s", message));
        this.kafkaTemplate.send(TOPIC, message);
    }

    @Autowired
    private KafkaTemplate <String, Double> bankKafkaTemplate;

    public void sendBankTx(String account, Double amount) {
        logger.info("send message to bank-transactions key {} and value {}", account, amount);
        this.bankKafkaTemplate.send(BANK_TOPIC, account, amount);
    }
}
