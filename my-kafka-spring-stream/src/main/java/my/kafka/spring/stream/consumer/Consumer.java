package my.kafka.spring.stream.consumer;

import my.kafka.bank.Topic;
import my.kafka.spring.stream.StateStore;
import my.kafka.spring.stream.message.Customer;
import my.kafka.spring.stream.message.EnrichedOrder;
import my.kafka.spring.stream.message.Order;
import my.kafka.spring.stream.message.Product;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Consumer {
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    StreamsBuilderFactoryBean defaultKafkaStreamsBuilder;

    public List<Customer> fetchAllCustomers() {
        List<Customer> result = new ArrayList<>();
        KeyValueIterator<String, Customer> all = customerStore().all();
        all.forEachRemaining(record -> {
            result.add(record.value);
        });
        return result;
    }

    protected ReadOnlyKeyValueStore<String, Customer> customerStore() {
        ReadOnlyKeyValueStore<String, Customer> store = defaultKafkaStreamsBuilder.getKafkaStreams().store(
                StateStore.CUSTOMER_STORE,
                QueryableStoreTypes.keyValueStore());
        return store;
    }

    public List<Product> fetchAllProdcuts() {
        List<Product> result = new ArrayList<>();
        KeyValueIterator<String, Product> all = productStore().all();
        all.forEachRemaining(record -> {
            result.add(record.value);
        });
        return result;
    }

    protected ReadOnlyKeyValueStore<String, Product> productStore() {
        ReadOnlyKeyValueStore<String, Product> store = defaultKafkaStreamsBuilder.getKafkaStreams().store(
                StateStore.PRODUCT_STORE,
                QueryableStoreTypes.keyValueStore());
        return store;
    }

    // consume
    @KafkaListener(topics = Topic.ORDER, groupId="monitor")
    public void consumeOrder(Order order) {
        logger.info("order -> {}", order);
    }

    @KafkaListener(topics = Topic.CUSTOMER, groupId="monitor")
    public void consumeCustomer(Customer customer) {
        logger.info("customer -> {}", customer);
    }

    @KafkaListener(topics = Topic.PRODUCT, groupId="monitor")
    public void consumeProduct(Product product) {
        logger.info("product -> {}", product);
    }

    @KafkaListener(topics = Topic.ENRICHED_ORDER, groupId="monitor")
    public void consumeEnrichedOrder(EnrichedOrder enrichedOrder) {
        logger.info("enrichedOrder -> {}", enrichedOrder);
    }

}
