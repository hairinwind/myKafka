package my.kafka.bank.consumer;

import my.kafka.bank.StateStore;
import my.kafka.bank.Topic;
import my.kafka.bank.message.BankTransaction;
import my.kafka.bank.producer.Producer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.retry.annotation.Backoff;

import static my.kafka.bank.Constants.isExternalAccount;
import static my.kafka.bank.message.BankTransactionStatus.CREATED;
import static my.kafka.bank.message.BankTransactionStatus.DEBIT_SUCCESS;
import static my.kafka.bank.message.BankTransactionStatus.FULFILLED;
import static my.kafka.bank.message.BankTransactionStatus.RETRY_BALANCE_NOT_ENOUGH;

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
public class MyKafkaStreamsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MyKafkaStreamsConfiguration.class);

    @Autowired
    private Producer producer;

    @DltHandler
    public void processMessage(BankTransaction bankTransaction) {
        logger.error("cannot process {}", bankTransaction);
    }

    // the containerFactory when business exception is thrown out from consumer@Bean
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory bankKafkaListenerContainerFactory(
//            KafkaTemplate<String, Object> bankKafkaTemplate,
//            ConsumerFactory<String, Object> consumerFactory) {
//        ConcurrentKafkaListenerContainerFactory<String, Double> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory);
//        factory.setErrorHandler(new SeekToCurrentErrorHandler(
                /* DeadLetterPublishingRecoverer is not needed as @RetryableTopic has same effect */
//                new DeadLetterPublishingRecoverer(bankKafkaTemplate), new FixedBackOff(0, 2L)));
//        return factory;
//    }

    @Bean
    public KStream<String, BankTransaction> alphaBankKStream(StreamsBuilder streamsBuilder) {
        JsonSerde<BankTransaction> valueSerde = new JsonSerde<>(BankTransaction.class);
        KStream<String, BankTransaction> stream = streamsBuilder.stream(Topic.TRANSACTION_RAW,
                Consumed.with(Serdes.String(), valueSerde));

        KTable<String, Double> balanceKtable = stream.map(this::mapKeyByStatus)  //(k,v) -> KeyValue.pair(v.getFromAccount(), v)
                .groupBy((account, bankTransaction) -> account, Grouped.with(Serdes.String(), valueSerde))
                .aggregate(
                        () -> 0D, /* initializer */
                        this::aggregateBalance /* aggregator */,
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(StateStore.BALANCE) /* state store name */
                                .withValueSerde(Serdes.Double()) /* state store value serde */
                );

        Topology topology = streamsBuilder.build();
        logger.info("topology: {}", topology.describe());

        return stream;
    }

    protected KeyValue<String, BankTransaction> mapKeyByStatus(String key, BankTransaction bankTransaction) {
        logger.debug("mapKeyByStatus {}", bankTransaction);
        if (bankTransaction.getStatus() == DEBIT_SUCCESS) {
            return KeyValue.pair(bankTransaction.getToAccount(), bankTransaction);
        } else {
            return KeyValue.pair(bankTransaction.getFromAccount(), bankTransaction);
        }
    }

    protected Double aggregateBalance(String key, BankTransaction bankTransaction, Double subtotal) {
        if (bankTransaction.getStatus() == CREATED ||
                bankTransaction.getStatus() == RETRY_BALANCE_NOT_ENOUGH) {
            if (isExternalAccount(key)) {
                //ignore external account
                bankTransaction.setStatus(DEBIT_SUCCESS);
                producer.sendRetryBankTransaction(bankTransaction);
                return 0D;
            }
            Double amount = bankTransaction.getAmount().doubleValue();
            // decrease money from fromAccount here
            if (subtotal - amount >= 0) {
                bankTransaction.setStatus(DEBIT_SUCCESS);
                producer.sendRetryBankTransaction(bankTransaction);
                logger.debug("balance decreased for {}", bankTransaction);
                return subtotal - amount;
            } else {
                logger.debug("balance {} is not enough {}", subtotal, bankTransaction);
                bankTransaction.setStatus(RETRY_BALANCE_NOT_ENOUGH);
                producer.sendRetryBankTransaction(bankTransaction);
                return subtotal;
            }
        } else if (bankTransaction.getStatus() == DEBIT_SUCCESS) {
            if (isExternalAccount(key)) {
                //ignore external account
                bankTransaction.setStatus(FULFILLED);
                producer.sendRetryBankTransaction(bankTransaction);
                return 0D;
            }
            bankTransaction.setStatus(FULFILLED);
            producer.sendRetryBankTransaction(bankTransaction);
            return subtotal + bankTransaction.getAmount().doubleValue();
        }
        return subtotal;
    }

    //KafkaListener for retry
    @RetryableTopic(attempts = "2",
            backoff = @Backoff(delay = 100, multiplier = 2, maxDelay = 1000))
    @KafkaListener(topics = Topic.TRANSACTION_RAW_RETRY)
    public void consume(BankTransaction bankTransaction) {
        switch(bankTransaction.getStatus()) {
            case FULFILLED:
                producer.sendCompletedBankTransaction(bankTransaction);
                break;
            case DEBIT_SUCCESS:
                producer.sendBankTransaction(bankTransaction);
                break;
            default:  /* RETRY_BALANCE_NOT_ENOUGH */
                bankTransaction.setRetriedTimes(bankTransaction.getRetriedTimes() + 1);
                if (bankTransaction.getRetriedTimes() % 2 == 0 && bankTransaction.getRetriedTimes() < 20) {
                    /* retry time is even, send back to topic to retry */
                    logger.info("retry...{}", bankTransaction);
                    producer.sendBankTransaction(bankTransaction);
                } else {
                    /* retry time is odd, throw out exception, so spring will delay it and call this method again */
                    logger.info("wait to retry...{}", bankTransaction);
                    throw new BalanceNotEnoughException(bankTransaction);
                }
        }
    }

}
