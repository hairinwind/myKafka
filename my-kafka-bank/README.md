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

## reset intermediate/auto-created topics
``` bin/kafka-streams-application-reset.sh --application-id alpha-bank --bootstrap-servers 192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092```

