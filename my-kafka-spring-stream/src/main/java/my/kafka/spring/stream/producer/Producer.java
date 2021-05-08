package my.kafka.spring.stream.producer;

import my.kafka.bank.Topic;
import my.kafka.spring.stream.message.Customer;
import my.kafka.spring.stream.message.Order;
import my.kafka.spring.stream.message.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCustomer(Customer customer) {
        this.kafkaTemplate.send(Topic.CUSTOMER, customer.getCustomerId(), customer);
    }

    public void sendOrder(Order order) {
        this.kafkaTemplate.send(Topic.ORDER, order.getOrderId(), order);
    }

    public void sendProduct(Product product) {
        this.kafkaTemplate.send(Topic.PRODUCT, product.getProductId(), product);
    }
}
