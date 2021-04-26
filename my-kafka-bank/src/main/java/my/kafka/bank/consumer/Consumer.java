package my.kafka.bank.consumer;

import my.kafka.bank.Topic;
import my.kafka.bank.message.AccountBalance;
import my.kafka.bank.message.BankTransaction;
import my.kafka.bank.message.BankTransactionInternal;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    private KStream<String, BankTransactionInternal> alphaBankInternalKStream;

    @Autowired
    private ReadOnlyKeyValueStore<String, Double> balanceStateStore;

    @KafkaListener(topics = Topic.TRANSACTION_RAW)
    public void consume(BankTransaction bankTransaction) {
        logger.info("bankTransaction created -> {}", bankTransaction);
    }

    @KafkaListener(topics = Topic.TRANSACTION_INTERNAL)
    public void consume(BankTransactionInternal txInternal) {
        logger.info("txInternal created -> {}", txInternal);
    }

    public AccountBalance getBalance(String accountNumber) {
//        final HostStoreInfo hostStoreInfo = streamsMetadataForStoreAndKey(BALANCE, accountNumber, new StringSerializer());
//        LOGGER.info("... the state is on this server: " + hostStoreInfo);
//        if (thisHost(hostStoreInfo)){
            return fetchLocalBalance(accountNumber);
//        } else {
//            return fetchRemoteBalance(hostStoreInfo, accountNumber);
//        }
    }

    public AccountBalance fetchLocalBalance(String account) {
        final Double value = balanceStateStore.get(account);
        if (value == null) {
            throw new RuntimeException("value is null");
        }
        return new AccountBalance(account, value);
    }
}
