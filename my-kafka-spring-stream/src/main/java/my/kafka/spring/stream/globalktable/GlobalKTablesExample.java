package my.kafka.spring.stream.globalktable;

import my.kafka.bank.Topic;
import my.kafka.spring.stream.StateStore;
import my.kafka.spring.stream.message.Customer;
import my.kafka.spring.stream.message.CustomerOrder;
import my.kafka.spring.stream.message.EnrichedOrder;
import my.kafka.spring.stream.message.Order;
import my.kafka.spring.stream.message.Product;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
public class GlobalKTablesExample {

    public static final Logger logger = LoggerFactory.getLogger(GlobalKTablesExample.class);

    @Bean
    public KStream<String, EnrichedOrder> alphaBankKStream(StreamsBuilder streamsBuilder) {
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        KStream<String, Order> ordersStream = streamsBuilder.stream(Topic.ORDER,
                Consumed.with(Serdes.String(), orderSerde));

        JsonSerde<Customer> customerSerde = new JsonSerde<>(Customer.class);
        final GlobalKTable<String, Customer> customers =
                streamsBuilder.globalTable(Topic.CUSTOMER,
                        Materialized.<String, Customer, KeyValueStore<Bytes, byte[]>>as(StateStore.CUSTOMER_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(customerSerde));

        JsonSerde<Product> productSerde = new JsonSerde<>(Product.class);
        final GlobalKTable<String, Product> products =
                streamsBuilder.globalTable(Topic.PRODUCT,
                        Materialized.<String, Product, KeyValueStore<Bytes, byte[]>>as(StateStore.PRODUCT_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(productSerde));

        final KStream<String, CustomerOrder> customerOrdersStream = ordersStream.join(customers,
                (orderId, order) -> order.getCustomerId(),
                (order, customer) -> CustomerOrder.builder().customer(customer).order(order).build());

        final KStream<String, EnrichedOrder> enrichedOrdersStream = customerOrdersStream.join(products,
                (orderId, customerOrder) -> customerOrder.productId(),
                (customerOrder, product) -> new EnrichedOrder(
                        product,
                        customerOrder.getCustomer(),
                        customerOrder.getOrder()));

        JsonSerde<EnrichedOrder> enrichedOrdersSerde = new JsonSerde<>(EnrichedOrder.class);
        enrichedOrdersStream.to(Topic.ENRICHED_ORDER, Produced.with(Serdes.String(), enrichedOrdersSerde));

        Topology topology = streamsBuilder.build();
        logger.info("topology: {}", topology.describe());

        return enrichedOrdersStream;
    }
}
