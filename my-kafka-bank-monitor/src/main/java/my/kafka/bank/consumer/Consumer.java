package my.kafka.bank.consumer;

import my.kafka.bank.Topic;
import my.kafka.bank.message.BankTransaction;
import my.kafka.bank.message.BankTransactionInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private static AtomicInteger transactionRawCount = new AtomicInteger(0);
    private static AtomicInteger transactionInternalCount = new AtomicInteger(0);

    @KafkaListener(topics = Topic.TRANSACTION_RAW, groupId="monitor")
    public void consume(BankTransaction bankTransaction) {
        logger.info("bankTransaction created -> {}", bankTransaction);
        transactionRawCount.addAndGet(1);
    }

    @KafkaListener(topics = Topic.TRANSACTION_INTERNAL, groupId="monitor")
    public void consume(BankTransactionInternal txInternal) {
        logger.info("txInternal created -> {}", txInternal);
        transactionInternalCount.addAndGet(1);
    }

    @KafkaListener(topics = Topic.TRANSACTION_RAW_RETRY, groupId="monitor")
    public void consumeBankTransactionRetry(BankTransaction bankTransaction) {
        logger.info("bank transaction retry message -> {}", bankTransaction);
    }

    @KafkaListener(topics = Topic.TRANSACTION_RAW_COMPLETED, groupId="monitor")
    public void consumeBankTransactionCompleted(BankTransaction bankTransaction) {
        logger.info("bank transaction completed -> {}", bankTransaction);
    }

    @KafkaListener(topics = Topic.TRANSACTION_RAW_RETRY_DLT, groupId="monitor")
    public void consumeBankTransactionRetryDLT(BankTransaction bankTransaction) {
        logger.info("bank transaction dead letter -> {}", bankTransaction);
    }

    public int getTransactionRawCount() {
        return transactionRawCount.get();
    }

    public int getTransactionInternalCount() {
        return transactionInternalCount.get();
    }

    public void resetCount() {
        transactionRawCount = new AtomicInteger(0);
        transactionInternalCount = new AtomicInteger(0);
    }
}
