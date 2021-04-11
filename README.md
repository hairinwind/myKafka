
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

## remote-state-store

## global-state-store


# Appendix
## commands of kafkacat
https://dev.to/de_maric/learn-how-to-use-kafkacat-the-most-versatile-kafka-cli-client-1kb4  

## kafka command line
```
kg --describe --group testConsumer001
```
kg is the alias of "/home/yao/Downloads/kafka_2.11-2.0.1/bin/kafka-consumer-groups.sh --bootstrap-server 192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092"

