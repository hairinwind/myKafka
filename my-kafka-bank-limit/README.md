## create topic with 3 partitions
```
<kafka_dir>/bin/kafka-topics.sh --zookeeper 192.168.49.2:32181 \
--create \
--replication-factor 2 \
--partitions 3 \
--topic alpha-bank-transactions-raw-retry
```

## projects
when the balance is less than the debit amount, the tx is not allowed. It will retry a couple of times and only be fulfilled when there is enough balance. 

## reference
https://github.com/spring-projects/spring-kafka/blob/main/spring-kafka-docs/src/main/asciidoc/retrytopic.adoc
