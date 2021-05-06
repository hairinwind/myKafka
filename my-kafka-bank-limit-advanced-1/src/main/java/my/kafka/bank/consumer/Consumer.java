package my.kafka.bank.consumer;

import my.kafka.bank.StateStore;
import my.kafka.bank.message.AccountBalance;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    StreamsBuilderFactoryBean defaultKafkaStreamsBuilder;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.host:localhost}")
    public String host;

    public AccountBalance getBalance(String accountNumber) {
        final HostStoreInfo hostStoreInfo = streamsMetadataForStoreAndKey(StateStore.BALANCE, accountNumber, new StringSerializer());
        logger.info("... the state is on this server: " + hostStoreInfo);
        if (thisHost(hostStoreInfo)){
            return fetchLocalBalance(accountNumber);
        } else {
            return fetchRemoteBalance(hostStoreInfo, accountNumber);
        }
    }

    protected <K> HostStoreInfo streamsMetadataForStoreAndKey(final String store,
                                                           final K key,
                                                           final Serializer<K> serializer) {
        final StreamsMetadata metadata = defaultKafkaStreamsBuilder.getKafkaStreams().metadataForKey(store, key, serializer);
        if (metadata == null) {
            throw new RuntimeException("metadata is null");
        }

        return new HostStoreInfo(metadata.host(),
                metadata.port(),
                metadata.stateStoreNames());
    }

    protected boolean thisHost(HostStoreInfo hostStoreInfo) {
        return hostStoreInfo.getHost().equals(host) && hostStoreInfo.getPort() == Integer.parseInt(serverPort);
    }

    AccountBalance fetchLocalBalance(String account) {
        final Double value = balanceStateStore().get(account);
//        if (value == null) {
//            throw new RuntimeException("value is null for account " + account);
//        }
        return new AccountBalance(account, value);
    }

    protected AccountBalance fetchRemoteBalance(HostStoreInfo hostStoreInfo, String accountNumber) {
        // target URL example is http://<host>:<port>/account/100001
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://" + hostStoreInfo.getHost()+":"+hostStoreInfo.getPort()+"/account/"+accountNumber;
        logger.info("...fetch remote state from " + url);
        return restTemplate.getForObject(url, AccountBalance.class);
    }

    public List<AccountBalance> fetchAllLocalBalances() {
        List<AccountBalance> result = new ArrayList<>();
        KeyValueIterator<String, Double> all = balanceStateStore().all();
        all.forEachRemaining(record -> {
            result.add(new AccountBalance(record.key, record.value));
        });
        return result;
    }

    protected ReadOnlyKeyValueStore<String, Double> balanceStateStore() {
        ReadOnlyKeyValueStore<String, Double> store = defaultKafkaStreamsBuilder.getKafkaStreams().store(
                StateStore.BALANCE,
                QueryableStoreTypes.keyValueStore());
        return store;
    }

}
