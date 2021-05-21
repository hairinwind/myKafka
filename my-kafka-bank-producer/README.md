## create topic with 3 partitions
```
<kafka_dir>/bin/kafka-topics.sh --zookeeper 192.168.49.2:32181 \
--create \
--replication-factor 2 \
--partitions 3 \
--topic alpha-bank-transactions-raw
```
```
<kafka_dir>/bin/kafka-topics.sh --zookeeper 192.168.49.2:32181 \
--create \
--replication-factor 2 \
--partitions 3 \
--topic alpha-bank-transactions-internal
```

## projects
my-kafka-bank is the project running the stream.  
my-kafka-bank-monitor is the consumer to check the messages were sent to the topics.  
my-kafka-bank-receiver is the reset service to receive client messages and pass it to kafka brokers. 

## performance
499500 bank transactions  
999000 bank internal transactions (split from bank transaction)  
total about 10 minutes to complete about 1 million transactions  
This is using one Rest service in front of the kafka broker.  

If I have 3 Rest service in front of kafka brokers to receive messages and send messages to kafka, kafka generates timeout error. 
