package my.kafka.bank.producer;

import my.kafka.bank.Topic;
import my.kafka.bank.message.BankTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBankTransaction(BankTransaction bankTransaction) {
        logger.debug("send to {}, {}", Topic.TRANSACTION_RAW, bankTransaction);
        kafkaTemplate.send(Topic.TRANSACTION_RAW, bankTransaction.getFromAccount(),bankTransaction);
    }

    public void sendRetryBankTransaction(BankTransaction bankTransaction) {
        logger.debug("send to {}, {}", Topic.TRANSACTION_RAW_RETRY, bankTransaction);
        kafkaTemplate.send(Topic.TRANSACTION_RAW_RETRY, bankTransaction.getFromAccount(),bankTransaction);
    }

    public void sendCompletedBankTransaction(BankTransaction bankTransaction) {
        logger.debug("send to {}, {}", Topic.TRANSACTION_RAW_COMPLETED, bankTransaction);
        kafkaTemplate.send(Topic.TRANSACTION_RAW_COMPLETED, bankTransaction.getFromAccount(),bankTransaction);
    }

}
