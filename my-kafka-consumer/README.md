
## the command to read the target topic of stream
```
./kafka-console-consumer.sh --bootstrap-server 192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092 \
--from-beginning --formatter kafka.tools.DefaultMessageFormatter \
--property print.key=true --property print.value=true \
--property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
--property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer \
--topic WordsWithCountsTopic 
```

## wait for the stream RUNNING
after ```streamts.start()```, the code need wait for the state RUNNING. Otherwise, you probably get the error 
``` Cannot get state store word-count because the stream thread is STARTING, not RUNNING ```  
Added this code to wait for the RUNNING state, or you can use the listerner from the article https://www.javaer101.com/en/article/5584501.html . 
```
while (true) {
    KafkaStreams.State state = streams.state();
    if (state == KafkaStreams.State.RUNNING) {
        System.out.println("streams.state " + state);
        break;
    } else {
        System.out.println("streams.state " + state);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
https://stackoverflow.com/questions/53534195/unable-to-open-store-for-kafka-streams-because-invalid-state


## TODO how to read the kTable