package my.kafka.mykafkaproducer.service;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class KafkaProducerService {

    private static Logger LOG = LoggerFactory.getLogger(KafkaProducerService.class);

    public static final String TEST_TOPIC = "test001";

    @Autowired
    @Qualifier("kafkaStringProducer")
    private KafkaProducer kafkaStringProducer;

    public void sendTestMessage() {
        String testMessage = "test message " + currentTime();

        String key = null;
        ProducerRecord<String, String> record = new ProducerRecord<>(TEST_TOPIC, key, testMessage);
        LOG.info("sent message: " + testMessage);
        kafkaStringProducer.send(record, new Callback() {
            // the callback is optional
            public void onCompletion(RecordMetadata metadata, Exception e) {
                if (e != null)
                    LOG.debug("Send failed for record {}", record, e);
            }
        });
    }

    private String currentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }


}
