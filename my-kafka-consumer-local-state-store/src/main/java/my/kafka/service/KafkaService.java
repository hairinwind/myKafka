package my.kafka.service;

import my.kafka.Constants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Service
@Scope("singleton")
public class KafkaService {

    public static final Logger LOG = LoggerFactory.getLogger(KafkaService.class);

    public static final String TOPIC = "bank-transactions";
    public static final String BALANCE = "balance";
    private KafkaStreams streams;

    public Properties config() {
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "transaction-balance");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "transaction-balance-my.kafka.bank.client");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP_SERVERS);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Double().getClass().getName());
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Use a temporary directory for storing state, which will be automatically removed after the test.
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/home/yao/myworkspace/kafka-data");
        return streamsConfiguration;
    }

    @PostConstruct
    public void postConstruct() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Double> txStream = builder.stream(TOPIC);

        KGroupedStream<String, Double> groupedByAccount = txStream
            .groupBy((account, amount) -> account, Grouped.with(Serdes.String(), Serdes.Double()));

//        Reducer<Double> reduceFunction = Double::sum;
        Reducer<Double> reduceFunction = (subtotal, amount) -> {
            // detect when the reducer is triggered
            System.out.println("reducer is running to add subtotal with amount..." + amount);
            return subtotal + amount;
        };
        groupedByAccount.reduce(reduceFunction,
                Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(BALANCE).withValueSerde(Serdes.Double()));

        txStream.foreach((account, amount) -> System.out.println("account: " + account + "| amount " + amount));
        streams = new KafkaStreams(builder.build(), config());

        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                streams.close();
            } catch (final Exception e) {
                // ignored
            }
        }));
    }

    /**
     * read from local state store
     * @param accountNumber
     * @return the balance
     */
    public Double getBalance(String accountNumber) {
        final ReadOnlyKeyValueStore<String, Double> store = streams.store(BALANCE, QueryableStoreTypes.keyValueStore());
        if (store == null) {
            throw new RuntimeException("store not found");
        }

        // Get the value from the store
        final Double value = store.get(accountNumber);
        if (value == null) {
            throw new RuntimeException("value is null");
        }
        return value;
    }

    public void send(String accountNumber, Double amount) {
        Properties config = new Properties();
        config.put("bootstrap.servers", Constants.BOOTSTRAP_SERVERS);
        config.put("acks", "all");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.DoubleSerializer");
        KafkaProducer<String, Double> kafkaStringProducer = new KafkaProducer<>(config);

        ProducerRecord<String, Double> record = new ProducerRecord<>(TOPIC, accountNumber, amount);
        LOG.info("sent message: " + accountNumber + " | " + amount);
        kafkaStringProducer.send(record);
    }

}