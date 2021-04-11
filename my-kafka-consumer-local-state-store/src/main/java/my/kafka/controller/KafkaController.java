package my.kafka.controller;

import my.kafka.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class KafkaController {

    @Autowired
    private KafkaService kafkaService;

    @GetMapping("/account/{accountNumber}")
    public Double getAccountBalance(@PathVariable String accountNumber) {
        return kafkaService.getBalance(accountNumber);
    }

    @PostMapping("/account/{accountNumber}")
    public String doTransaction(@PathVariable String accountNumber, @RequestParam Double amount) {
        kafkaService.send(accountNumber, amount);
        return "SUCCESS";
    }

    @GetMapping("/status")
    public String getStatus() {
        return "up";
    }

}
