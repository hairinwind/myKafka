## create topic with 3 partitions before test
```
<kafka_dir>/bin/kafka-topics.sh --create \
--zookeeper 192.168.49.2:32181 \
--replication-factor 2 \
--partitions 3 \
--topic test_partition
```

## run it with argument "send"
It can produce 4 messages, the first 3 messages don't have partition and let kafka determine the partition.  
The 4th message specified one partition. 

## run one instance with argument "consume"
it shall consume all the messages in all partitions  
shall see the log 
```
Adding newly assigned partitions: test_partition-1, test_partition-2, test_partition-0
```

## run the second instances with argument "consume"
see the log 
```
Adding newly assigned partitions: test_partition-2
```
And the previous running instance, see the log 
```
Adding newly assigned partitions: test_partition-1, test_partition-0
```
run the instance with argument "send", I can see one consumer instance consumer the messages on partition 0 and 1, and the other one consumes the messages on partition 2. 

## run the third instance with argument "consume"
now, I can see each instance is on one partition.  
After run "send" message, I can see each consumer instance only take messages from one partition.

## view the active consumers in one group
```
kg --describe --group testConsumer001
```
kg is the alias of "/home/yao/Downloads/kafka_2.11-2.0.1/bin/kafka-consumer-groups.sh --bootstrap-server 192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092"


