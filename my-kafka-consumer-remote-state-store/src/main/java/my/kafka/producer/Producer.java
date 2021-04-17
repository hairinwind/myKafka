package my.kafka.producer;

import my.kafka.Constants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.apache.kafka.streams.state.RocksDBConfigSetter.LOG;

@Service
public class Producer {

    private static final String TOPIC = "bank-transactions-3-partitions";

    public RecordMetadata send(String accountNumber, Double amount) throws ExecutionException, InterruptedException {
        Properties config = new Properties();
        config.put("bootstrap.servers", Constants.BOOTSTRAP_SERVERS);
        config.put("acks", "all");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.DoubleSerializer");
        KafkaProducer<String, Double> kafkaStringProducer = new KafkaProducer<>(config);

        ProducerRecord<String, Double> record = new ProducerRecord<>(TOPIC, accountNumber, amount);
        LOG.info("sent message: " + accountNumber + " | " + amount);
        Future<RecordMetadata> futureResult =  kafkaStringProducer.send(record);

        while(!futureResult.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return futureResult.get();
    }

}
