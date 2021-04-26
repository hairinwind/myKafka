## create topic with 3 partitions
```
<kafka_dir>/bin/kafka-topics.sh --create \
--zookeeper 192.168.49.2:32181 \
--replication-factor 2 \
--partitions 3 \
--topic alpha-bank-transactions-raw
```