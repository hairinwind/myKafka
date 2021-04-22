package my.kafka.spring.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapAddress;

    @Value("${spring.kafka.consumer.group-id}")
    private String userGroupId;

    // Consume user objects from Kafka
    // more detail, see https://howtodoinjava.com/kafka/multiple-consumers-example/
    public ConsumerFactory<String, Double> bankConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, userGroupId);
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new DoubleDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Double> bankKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Double> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bankConsumerFactory());
        return factory;
    }
}
