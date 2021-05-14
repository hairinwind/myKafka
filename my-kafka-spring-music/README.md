## my-kafka-spring-music
this is practising the sample code from https://github.com/confluentinc/kafka-streams-examples/blob/0bd6244a4627db32df365a7b5d00d069eedd9a0c/src/main/java/io/confluent/examples/streams/interactivequeries/kafkamusic/KafkaMusicExample.java

## Topics 
Topics for GlobalKTablesExample
```
bin/kafka-topics --zookeeper localhost:2181 \
    --create --topic play-events --partitions 4 --replication-factor 1
bin/kafka-topics --zookeeper localhost:2181 \
    --create --topic song-feed --partitions 4 --replication-factor 1
```



