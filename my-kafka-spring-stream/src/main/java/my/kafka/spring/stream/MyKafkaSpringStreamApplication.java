package my.kafka.spring.stream;

import my.kafka.spring.stream.consumer.Consumer;
import my.kafka.spring.stream.message.Customer;
import my.kafka.spring.stream.message.Order;
import my.kafka.spring.stream.message.PageView;
import my.kafka.spring.stream.message.Product;
import my.kafka.spring.stream.message.UserProfile;
import my.kafka.spring.stream.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class MyKafkaSpringStreamApplication {

    @Autowired
    private Consumer consumer;

    @Autowired
    private Producer producer;

    public static void main(String[] args) {
        SpringApplication.run(MyKafkaSpringStreamApplication.class, args);
    }

    @PostMapping(value = "/customer")
    public void sendMessageToKafkaTopic(@RequestParam("customerId") String customerId,
                                        @RequestParam("name") String customerName) {
        Customer customer = Customer.builder()
                .customerId(customerId)
                .name(customerName)
                .build();
        this.producer.sendCustomer(customer);
    }

    @PostMapping(value = "/product")
    public void sendProduct(@RequestParam("productId") String productId,
                            @RequestParam("name") String productName) {
        Product product = Product.builder()
                .productId(productId)
                .name(productName)
                .build();
        this.producer.sendProduct(product);
    }

    @PostMapping(value = "/order")
    public void sendOrder(@RequestParam("orderId") String orderId, @RequestParam("customerId") String customerId,
                          @RequestParam("productId") String productId, @RequestParam("quantity") int quantity) {
        Order order = Order.builder()
                .orderId(orderId)
                .customerId(customerId)
                .productId(productId)
                .quantity(quantity)
                .build();
        this.producer.sendOrder(order);
    }

    @PostMapping(value = "/userProfile")
    public void sendUserProfile(@RequestParam("userId") String userId,
                            @RequestParam("region") String region) {
        UserProfile userProfile = UserProfile.builder()
                .userId(userId)
                .region(region)
                .build();
        this.producer.sendUserProfile(userProfile);
    }

    @PostMapping(value = "/sendPageViewTestData")
    public void sendPageViewTestData() {
        producer.sendPageView(PageView.builder().page("index.html").userId("Jason").build());
        producer.sendPageView(PageView.builder().page("index.html").userId("Chris").build());
        producer.sendPageView(PageView.builder().page("index.html").userId("ZhangSan").build());
    }

    @GetMapping(value = "/customers")
    public List<Customer> fetchAllCustomers() {
        return consumer.fetchAllCustomers();
    }

    @GetMapping(value = "/products")
    public List<Product> fetchAllProducts() {
        return consumer.fetchAllProdcuts();
    }
}
