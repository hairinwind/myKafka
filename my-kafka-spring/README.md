# the project to use spring-kafka

spring-kafka is different with spring-cloud-stream. Don't mix them together. 

## reference
- https://howtodoinjava.com/kafka/multiple-consumers-example/   
- https://www.baeldung.com/spring-kafka  
- https://www.confluent.io/blog/apache-kafka-spring-boot-application/?utm_medium=sem&utm_source=google&utm_campaign=ch.sem_br.nonbrand_tp.prs_tgt.kafka_mt.mbm_rgn.namer_lng.eng_dv.all&utm_term=%2Bkafka%20%2Bspring&creative=&device=c&placement=&gclid=CjwKCAjwmv-DBhAMEiwA7xYrd6wTE1YQObTaXR2BaouC75PCuMmqiQn4S940zvIKXmo60mYeYGBz_hoC8BoQAvD_BwE  
- https://docs.spring.io/spring-kafka/reference/html/#reference
- https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-1-error-handling-message-conversion-transaction-support/

## use default 
if the project is simple, just send or consume messages from one topic.  
The default setting can be used and spring has the related beans ready. 
```
spring:
  kafka:
    consumer:
      bootstrap-servers
      ...
    my.kafka.bank.producer:
       ...
```
my.kafka.spring.consumer.Consumer.consume(String message) is using the spring default containerFactory. The default container Factory is taking the configuration from application.yml.  
my.kafka.spring.my.kafka.bank.producer.Producer.sendMessage(String message) is using the spring default kafkaTemplate. The default kafkaTemplate is taking the configuration from application.yml.  

## use custom factory
For real application, you need visit multiple topics. It probably needs different config props, like different Serializer and Deserializer.  
### consumer
Two beans and one listener is needed here:  
my.kafka.spring.consumer.KafkaConsumerConfig.bankConsumerFactory  
my.kafka.spring.consumer.KafkaConsumerConfig.bankKafkaListenerContainerFactory  
Then on the listener, specify the containerFactory
```
    @KafkaListener(topics = "bank-transactions", groupId = "testConsumer007",
            containerFactory = "bankKafkaListenerContainerFactory")
    public void consumeWithErrors(ConsumerRecord<String, Double> record) throws IOException {
        ...
    }
```

### my.kafka.bank.producer
Two beans is needed:
my.kafka.spring.my.kafka.bank.producer.KafkaProducerConfig.producerFactory(), which specify serializer.  
my.kafka.spring.my.kafka.bank.producer.KafkaProducerConfig.kafkaTemplate()  
I found once I create a custom kafkaTemplate, the default one from Spring is not working anymore. If I want to use it, I have create the bean in code. 

## dead letter topic
https://www.confluent.io/blog/kafka-connect-deep-dive-error-handling-dead-letter-queues/  
If there is any error in consumer, an exception is thrown out. my.kafka.spring.consumer.Consumer.consumeWithErrors(...)  
With these settings in ContainerFactory, Spring would send the error record to the dead letter topic with name <YOUR_TOPIC>.DLT  
```
factory.setErrorHandler(new SeekToCurrentErrorHandler(
                new DeadLetterPublishingRecoverer(bankKafkaTemplate), new FixedBackOff(0, 2L)));
```
To test it, ```curl -X POST -F 'amount=1' localhost:9000/sendBankTx/100002```  
It shall appear in DLT topic. 

## error in Deserialization
https://www.confluent.io/blog/spring-kafka-can-your-kafka-consumers-handle-a-poison-pill/  
If the message cannot be deserialized, spring provides ErrorHandlingDeserializer to wrap the real Deserializer.
```
return new DefaultKafkaConsumerFactory<>(props,
                new ErrorHandlingDeserializer(new StringDeserializer()),
                new ErrorHandlingDeserializer(new DoubleDeserializer()));
```
a kafkaTemplate to send byte[] is provided
```
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
```
Don't mix the deserialization exception with the business exception on same topic, it does not work. For more detail, see my.kafka.spring.consumer.KafkaConsumerConfig.bankDeserializationConsumerFactory()  
To test it, ```curl -X POST -F 'amount=a' localhost:9000/sendWrong/100003```
It shall appear in the DLT topic  

## TODO spring-kafka stream
https://docs.spring.io/spring-kafka/reference/html/#basics
