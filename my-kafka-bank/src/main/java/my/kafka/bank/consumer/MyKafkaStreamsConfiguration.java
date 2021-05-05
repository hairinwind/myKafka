package my.kafka.bank.consumer;

import my.kafka.bank.Constants;
import my.kafka.bank.StateStore;
import my.kafka.bank.Topic;
import my.kafka.bank.message.BankTransaction;
import my.kafka.bank.message.BankTransactionInternal;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.LinkedList;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
public class MyKafkaStreamsConfiguration {

    @Bean
    public KStream<String, BankTransaction> alphaBankKStream(StreamsBuilder streamsBuilder) {
        JsonSerde<BankTransaction> valueSerde = new JsonSerde<>(BankTransaction.class);
        KStream<String, BankTransaction> stream = streamsBuilder.stream(Topic.TRANSACTION_RAW,
                Consumed.with(Serdes.String(), valueSerde));
        stream.flatMap((k, v) -> {
            List<BankTransactionInternal> txInternals = BankTransactionInternal.splitBankTransaction(v);
            List<KeyValue<String, BankTransactionInternal>> result = new LinkedList<>();
            result.add(KeyValue.pair(v.getFromAccount(), txInternals.get(0)));
            result.add(KeyValue.pair(v.getToAccount(), txInternals.get(1)));
            return result;
        }).filter((k, v) -> !Constants.EXTERNAL_ACCOUNT.equalsIgnoreCase(k))
                .to(Topic.TRANSACTION_INTERNAL, Produced.with(Serdes.String(), new JsonSerde<>()));
        return stream;
    }

    @Bean
    public KStream<String, BankTransactionInternal> alphaBankInternalKStream(StreamsBuilder streamsBuilder) {
        JsonSerde<BankTransactionInternal> valueSerde = new JsonSerde<>(BankTransactionInternal.class);
        KStream<String, BankTransactionInternal> stream = streamsBuilder.stream(Topic.TRANSACTION_INTERNAL,
                Consumed.with(Serdes.String(), valueSerde));

        KGroupedStream<String, Double> groupedByAccount = stream
                .map((k,v) -> KeyValue.pair(k, v.getAmount()))
                .groupBy((account, amount) -> account, Grouped.with(Serdes.String(), Serdes.Double()));
        Reducer<Double> reduceFunction = (subtotal, amount) -> {
            // detect when the reducer is triggered
            System.out.println("...reducer is running to add subtotal with amount..." + amount);
            return subtotal + amount;
        };
        //Double::sum
        groupedByAccount.reduce(reduceFunction,
                Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(StateStore.BALANCE)
                        .withValueSerde(Serdes.Double()));

        return stream;
    }

}
