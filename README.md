
## my-kafka-producer
this is the project to send a message to topic "test001"

## my-kafka-consumer
this is the project to consume messages from topic "test001"

## my-kafka-partition
test topic with multiple partitions, and the consumers in same group are taking messages from different partition  
kafka is doing the load balance on the consumers in same group

## my-kafka-consumer-local-state-store
this is the project to do the bank transaction.   
The result is sum all transactions as balance. 
It supports to read the "balance" result from the same stream.  
Verify if it redo the calculating, after delete the state folder and restart kafka cluster. (NO...)

## my-kafka-consumer-remote-state-store
the prvious project is to read the state from local-store.  
If the topic is multiple partitions and there are multiple consumers are running, they take messages from one partition. If still reading state from local, you may not get it. As the state mighht be on another consumer instance. 
this is the project to get the state from remote state store. 

## my-kafka-spring
use spring-kafka (this is different with spring-cloud-stream)  
https://www.baeldung.com/spring-kafka
https://www.confluent.io/blog/apache-kafka-spring-boot-application/?utm_medium=sem&utm_source=google&utm_campaign=ch.sem_br.nonbrand_tp.prs_tgt.kafka_mt.mbm_rgn.namer_lng.eng_dv.all&utm_term=%2Bkafka%20%2Bspring&creative=&device=c&placement=&gclid=CjwKCAjwmv-DBhAMEiwA7xYrd6wTE1YQObTaXR2BaouC75PCuMmqiQn4S940zvIKXmo60mYeYGBz_hoC8BoQAvD_BwE  

## my-kafka-spring-cloud-stream
this project is to use spring-cloud-stream + kafka.  
It is a simple project, one producer and one consumer.  
reference doc:
- https://cloud.spring.io/spring-cloud-static/spring-cloud-stream-binder-kafka/2.2.0.RC1/spring-cloud-stream-binder-kafka.html#_apache_kafka_binder  
- https://github.com/spring-cloud/spring-cloud-stream-samples  

# Appendix
## commands of kafkacat
https://dev.to/de_maric/learn-how-to-use-kafkacat-the-most-versatile-kafka-cli-client-1kb4  

## kafka command line
```
kg --describe --group testConsumer001
```
kg is the alias of "/home/yao/Downloads/kafka_2.11-2.0.1/bin/kafka-consumer-groups.sh --bootstrap-server 192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092"

