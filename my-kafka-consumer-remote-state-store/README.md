
## create a 3 partition topic
```
<kafka_dir>/bin/kafka-topics.sh --create \
--zookeeper 192.168.49.2:32181 \
--replication-factor 2 \
--partitions 3 \
--topic bank-transactions-3-partitions
```

## create the producer and send some messages 
create the producer my.kafka.producer.Producer  
send some messages:  
curl -X POST localhost:8080/account/100001?amount=1  
curl -X POST localhost:8080/account/100002?amount=2  
curl -X POST localhost:8080/account/100003?amount=3  
make sure there are messages on different partitions.  

## create consumers and run multiple instances 
create consumers which is still using the local state store.
run 3 instances to let one instance linked to one partition

## get balance on each instance
for example:
```
curl -X POST localhost:9001/account/100001?amount=1
```
record is sent to partition 1

Check which instance is connected to which partition. search "Adding newly assigned partitions: " in console log
the instance on port 9001 was connected to partition-0  
the 9002 -> partition-1  
the 9003 -> partition-2 

Now I call "getBalance" ```curl -X GET localhost:9001/account/100001```  
I got the error "status":500,"error":"Internal Server Error"

```
curl -X GET localhost:9002/account/100001
```
returns 1

```
curl -X GET localhost:9003/account/100001
```
error "status":500,"error":"Internal Server Error"

Check the log of the instance returns 500 error, there is exception
```
java.lang.RuntimeException: value is null
	at my.kafka.consumer.Consumer.getBalance(Consumer.java:76) ~[classes/:na]
...
```

**Only the instance connected to the right partition can get the balance.**

## try query remote state store