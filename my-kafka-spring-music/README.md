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

## To Test
initial songs
```
curl -X POST localhost:9000/initialSongs
```
send play event, change songId and sent it multiple times
```
curl -d "songId=1&duration=40000" localhost:9000/playEvent
```
check the log to see how the data flows

read TOP_FIVE_SONGS_BY_GENRE_STORE
```
curl localhost:9000/top5ByGenre
```
read TOP_FIVE_SONGS_STORE
```
curl localhost:9000/top5 | jq
```
read SONG_PLAY_COUNT_STORE
```
curl localhost:9000/songPlayCount | jq
```


