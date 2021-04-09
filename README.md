
## my-kafka-producer
this is the project to send a message to topic "test001"

## my-kafka-consumer
this is the project to consume messages from topic "test001"

## my-kafka-partition
test topic with multiple partitions, and the consumers in same group on different partition

## my-kafka-consumer-local-state-store
this is the project to do the word count on the messages.  
The result is materialized to "word-count".  
It supports to read the "word-count" result from the same stream which is doing the word count.  
Verify if it redo the count, after restart...

## remote-state-store

## global-state-store