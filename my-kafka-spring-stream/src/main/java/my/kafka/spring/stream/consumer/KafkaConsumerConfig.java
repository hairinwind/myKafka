package my.kafka.spring.stream.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

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
    @Bean
    public ConsumerFactory<String, Double> bankConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new DoubleDeserializer());
    }

    // the containerFactory when business exception is thrown out from consumer@Bean
    @Bean
    public ConcurrentKafkaListenerContainerFactory bankKafkaListenerContainerFactory(
            KafkaTemplate<String, Double> bankKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, Double> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bankConsumerFactory());
        factory.setErrorHandler(new SeekToCurrentErrorHandler(
                new DeadLetterPublishingRecoverer(bankKafkaTemplate), new FixedBackOff(0, 2L)));
        return factory;
    }

    // the consumerFactory to handle exception in Deserialization
    // don't mix business exception with deserialize exception on the same topic
    // for business exception, spring tries to send the record<Sting, Double> to the DLT topic
    // but the bankDeserializationKafkaListenerContainerFactory below is configured to use KafkaTemplate<String, byte[]>
    // which can send error record when there is deserialization error
    // In this case, it needs two topic, topic1 -> topic2
    // hook ErrorHandlingDeserializer on topic1
    // then all records on topic2 are <String, Double>
    // hook KafkaTemplate<String, Double> to topic2, when business error happens, that kafkaTemplate would be used to send the record to DLT
    @Bean
    public DefaultKafkaConsumerFactory bankDeserializationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props,
                new ErrorHandlingDeserializer(new StringDeserializer()),
                new ErrorHandlingDeserializer(new DoubleDeserializer()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory bankDeserializationKafkaListenerContainerFactory(
            KafkaTemplate<String, byte[]> bytesKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, Double> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bankDeserializationConsumerFactory());
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(bytesKafkaTemplate);
        SeekToCurrentErrorHandler errorHandler = new SeekToCurrentErrorHandler(
                recoverer, new FixedBackOff(0, 2L));
        factory.setErrorHandler(errorHandler);
        return factory;
    }

    // the Beans below is to consumer the String - byte[] from .DLT topic
    public ConsumerFactory<String, byte[]> bytesConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new ByteArrayDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> bytesKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bytesConsumerFactory());
        return factory;
    }
}
