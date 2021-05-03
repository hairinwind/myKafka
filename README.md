
## my-kafka-my.kafka.bank.producer
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
the previous project is to read the state from local-store.  
If the topic is multiple partitions and there are multiple consumers are running, they take messages from one partition. If still reading state from local, you may not get it. As the state mighht be on another consumer instance. 
this is the project to get the state from remote state store. 

## my-kafka-spring
use spring-kafka (this is different with spring-cloud-stream)

## my-kafka-spring-cloud-stream
this project is to use spring-cloud-stream + kafka.  
It is a simple project, one my.kafka.bank.producer and one consumer.  
reference doc:
- https://cloud.spring.io/spring-cloud-static/spring-cloud-stream-binder-kafka/2.2.0.RC1/spring-cloud-stream-binder-kafka.html#_apache_kafka_binder  
- https://github.com/spring-cloud/spring-cloud-stream-samples

## my-kafka-bank, my-kafka-bank-monitor and my-kafka-bank-receiver
- receive transactions: fromAccount / toAccount / amount / time / tx_uuid
- flatTransform 1 transactions into 2 transactions, one on fromAccount and the other one is on toAccount, and executeInTransaction
- stream to calculate balance 
- add 1000 accounts, with initial deposit tx
- for each account, create transactions to other account. Test the performance
my-kafka-bank-client contains the test client. 
  
## my-kafka-bank-limit
- set up the limit, for example, if the debit amount > account balance, the transaction is not allowed. It shall be sent to another topic and let it retry later.


## exception handling

## unit test
@EmbeddedKafka

## Kafka Processor API
Kafka processor API is low level API.  
Kafka DSL (stream language) is high level API.  
https://kafka.apache.org/10/documentation/streams/developer-guide/processor-api.html  
http://mkuthan.github.io/blog/2017/11/02/kafka-streams-dsl-vs-processor-api/  
https://medium.com/@ceyhunuzngl/kafka-stream-processor-api-in-spring-boot-4e251067a58f#_=_  
TODO...

# Appendix
## commands of kafkacat
https://dev.to/de_maric/learn-how-to-use-kafkacat-the-most-versatile-kafka-cli-my.kafka.bank.client-1kb4  

## kafka command line
```
kg --describe --group testConsumer001
```
kg is the alias of "/home/yao/Downloads/kafka_2.11-2.0.1/bin/kafka-consumer-groups.sh --bootstrap-server 192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092"

check the offset
```
bin/kafka-consumer-groups.sh --bootstrap-server 192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092 --group monitor --describe
```

reset intermediate/auto-created topics
``` bin/kafka-streams-application-reset.sh --application-id alpha-bank --bootstrap-servers 192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092```

## reference
https://www.confluent.io/blog/kafka-streams-tables-part-3-event-processing-fundamentals/  

spring-kafka default properties  
https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html#spring.kafka.admin.client-id  

