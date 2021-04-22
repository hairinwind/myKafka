package my.kafka.spring;

import my.kafka.spring.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MyKafkaSpringApplication {

    @Autowired
    private Producer producer;

    public static void main(String[] args) {
        SpringApplication.run(MyKafkaSpringApplication.class, args);
    }

    @PostMapping(value = "/send")
    public void sendMessageToKafkaTopic(@RequestParam("message") String message) {
        this.producer.sendMessage(message);
    }

    @PostMapping(value = "/sendBankTx/{account}")
    public void sendBankTx(@PathVariable String account, @RequestParam("amount") Double amount) {
        this.producer.sendBankTx(account, amount);
    }

}
