package my.kafka.controller;

import my.kafka.consumer.Consumer;
import my.kafka.consumer.HostStoreInfo;
import my.kafka.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.errors.NotFoundException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class MyKafkaRemoteStateController {

    @Autowired
    private Consumer consumer;

    @Autowired
    private Producer producer;

    @GetMapping("/account/{accountNumber}")
    public Double getAccountBalance(@PathVariable String accountNumber) {
        return consumer.getBalance(accountNumber);
    }

    @PostMapping("/account/{accountNumber}")
    public String doTransaction(@PathVariable String accountNumber, @RequestParam Double amount) throws ExecutionException, InterruptedException {
        RecordMetadata recordMetadata = producer.send(accountNumber, amount);
        return "record is sent to partition " + recordMetadata.partition();
    }

}
