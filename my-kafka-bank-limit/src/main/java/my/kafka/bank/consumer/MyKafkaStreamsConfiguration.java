package my.kafka.bank.consumer;

import my.kafka.bank.Constants;
import my.kafka.bank.StateStore;
import my.kafka.bank.Topic;
import my.kafka.bank.message.AccountBalance;
import my.kafka.bank.message.BankTransaction;
import my.kafka.bank.message.BankTransactionInternal;
import my.kafka.bank.producer.Producer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
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

import java.util.LinkedList;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
public class MyKafkaStreamsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MyKafkaStreamsConfiguration.class);

    @Autowired
    private Consumer consumer;

    @Autowired
    private Producer producer;

    @DltHandler
    public void processMessage(BankTransaction bankTransaction) {
        logger.error("cannot process {}", bankTransaction);
    }

    @Bean
    public KStream<String, BankTransaction> alphaBankKStream(StreamsBuilder streamsBuilder) {
        JsonSerde<BankTransaction> valueSerde = new JsonSerde<>(BankTransaction.class);
        KStream<String, BankTransaction> stream = streamsBuilder.stream(Topic.TRANSACTION_RAW,
                Consumed.with(Serdes.String(), valueSerde));

        /**
         * This does not work when high concurrency
         * the state store balance has some delay
         *
         * As the balance could be on remote, it shall be avoided.
         */
        KStream<String, BankTransaction>[] branches = stream.branch(
                (key, value) -> isBalanceEnough(value),
                (key, value) -> true                 /* all other records  */
        );

        branches[0].flatMap((k, v) -> {
            List<BankTransactionInternal> txInternals = BankTransactionInternal.splitBankTransaction(v);
            List<KeyValue<String, BankTransactionInternal>> result = new LinkedList<>();
            result.add(KeyValue.pair(v.getFromAccount(), txInternals.get(0)));
            result.add(KeyValue.pair(v.getToAccount(), txInternals.get(1)));
            return result;
        }).filter((k, v) -> !Constants.EXTERNAL_ACCOUNT.equalsIgnoreCase(k))
                .map((k,v) -> KeyValue.pair(k, v.getAmount()))
                .groupBy((account, amount) -> account, Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum,
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(StateStore.BALANCE).withValueSerde(Serdes.Double()));

        branches[1].to(Topic.TRANSACTION_RAW_RETRY, Produced.with(Serdes.String(), new JsonSerde<>()));
        return stream;
    }

    private boolean isBalanceEnough(BankTransaction bankTransaction) {
        if (bankTransaction.getFromAccount().equalsIgnoreCase("external")) {
            return true;
        }
        AccountBalance balance = consumer.getBalance(bankTransaction.getFromAccount());
        if (balance == null || balance.getBalance() == null) {
            return false;
        }
        if (balance.getBalance().doubleValue() >= bankTransaction.getAmount().doubleValue()) {
            logger.info("balance is enough for {}", bankTransaction);
        } else {
            logger.error("balance is {} and is not enough {}", balance, bankTransaction);
//            throw new BalanceNotEnoughException(balance, bankTransaction);
        }
        return balance.getBalance().doubleValue() >= bankTransaction.getAmount().doubleValue();
    }

    //KafkaListener for retry
    @RetryableTopic(attempts = "10",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000))
    @KafkaListener(topics = Topic.TRANSACTION_RAW_RETRY)
    public void consume(BankTransaction bankTransaction) {
        if (isBalanceEnough(bankTransaction)) {
            producer.sendBankTransaction(bankTransaction);
        } else {
            logger.warn("balance not enough, retry...{}", bankTransaction);
            throw new BalanceNotEnoughException(bankTransaction);
        }
    }
    // ====================
}
