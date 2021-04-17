package my.kafka.consumer;

import my.kafka.Constants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class Consumer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
    private static final String TOPIC = "bank-transactions-3-partitions";
    public static final String BALANCE = "balance-3-partitions";

    private KafkaStreams streams;

    @Value("${instance.index}")
    private String instanceIndex;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.host:localhost}")
    public String host;

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

        Collection<StreamsMetadata> streamsMetadata = streams.allMetadata();
        System.out.println(streamsMetadata.size());
    }

    /**
     * read from local state store
     * @param accountNumber
     * @return the balance
     */
    public Double getBalance(String accountNumber) {
        final HostStoreInfo hostStoreInfo = streamsMetadataForStoreAndKey(BALANCE, accountNumber, new StringSerializer());
        LOGGER.info("... the state is on this server: " + hostStoreInfo);
        if (thisHost(hostStoreInfo)){
            return fetchLocalBalance(accountNumber);
        } else {
            return fetchRemoteBalance(hostStoreInfo, accountNumber);
        }
    }

    private boolean thisHost(HostStoreInfo hostStoreInfo) {
        return hostStoreInfo.getHost().equals(host) && hostStoreInfo.getPort() == Integer.parseInt(serverPort);
    }

    private Double fetchRemoteBalance(HostStoreInfo hostStoreInfo, String accountNumber) {
        // target URL example is http://<host>:<port>/account/100001
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://" + hostStoreInfo.getHost()+":"+hostStoreInfo.getPort()+"/account/"+accountNumber;
        LOGGER.info("...fetch state from " + url);
        return restTemplate.getForObject(url, Double.class);
    }

    protected Double fetchLocalBalance(String accountNumber) {
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

    private Properties config() {
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "transaction-balance-3-partitions");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "transaction-balance-client-3-partitions");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.BOOTSTRAP_SERVERS);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Double().getClass().getName());
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Use a temporary directory for storing state, which will be automatically removed after the test.
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/home/yao/myworkspace/kafka-data/" + instanceIndex);

        // Set the unique RPC endpoint of this application instance through which it
        // can be interactively queried.  In a real application, the value would most
        // probably not be hardcoded but derived dynamically.
        String rpcEndpoint = host + ":" + serverPort;
        streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, rpcEndpoint);

        return streamsConfiguration;
    }

    // the functions below are to support RPC
    public <K> HostStoreInfo streamsMetadataForStoreAndKey(final String store,
                                                           final K key,
                                                           final Serializer<K> serializer) {
        // Get metadata for the instances of this Kafka Streams application hosting the store and
        // potentially the value for key
        // the return host and port is APPLICATION_SERVER_CONFIG set in config, when each stream is started
        final StreamsMetadata metadata = streams.metadataForKey(store, key, serializer);
        if (metadata == null) {
            throw new RuntimeException("value is null");
        }

        return new HostStoreInfo(metadata.host(),
                metadata.port(),
                metadata.stateStoreNames());
    }
}
