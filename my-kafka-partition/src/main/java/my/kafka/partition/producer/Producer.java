package my.kafka.partition.producer;

import my.kafka.partition.Constants;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

@Service
public class Producer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

    public void sendMessages() {
        IntStream.range(0, 3).boxed().map(String::valueOf).forEach(this::sendMessage);
        sendMessageToPartition(1);
    }

    /**
     * let kafka to determine the target partition
     */
    protected void sendMessage(String key) {
        try(final KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(config())) {
            String testMessage = "test message" + key + " " + currentTime();
            ProducerRecord<String, String> record = new ProducerRecord<>(Constants.PARTITION_TOPIC, key, testMessage);
            kafkaProducer.send(record);
        }
    }

    /**
     * specify the target partition
     */
    protected void sendMessageToPartition(int partition) {
        try (final KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(config())) {
            String testMessage = "test message on partition " + partition + " " + currentTime();
            String key = String.valueOf(partition);
            ProducerRecord<String, String> record = new ProducerRecord<>(Constants.PARTITION_TOPIC, partition, key, testMessage);

            Future<RecordMetadata> send = kafkaProducer.send(record);
        }
        // put the kafkaProducer in try {}, it would wait until it is closed
        // otherwise the main thread is done and the child threads are killed, the message may not be sent.
        // with the try code above, no need to have this "sleep" code
//        // sleep to allow the message sent
//        while (!send.isDone()) {
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    protected Properties config() {
        Properties config = new Properties();
        config.put("bootstrap.servers", Constants.BOOTSTRAP_SERVERS);
        config.put("acks", "all");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return config;
    }

    protected String currentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}
