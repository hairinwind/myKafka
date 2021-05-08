## my-kafka-spring-stream
this is practising the sample code from https://github.com/confluentinc/kafka-streams-examples

## Topics 
```
bin/kafka-topics --zookeeper localhost:2181 \
    --create --topic order --partitions 3 --replication-factor 1
bin/kafka-topics --zookeeper localhost:2181\
    --create --topic customer --partitions 3 --replication-factor 1
bin/kafka-topics --zookeeper localhost:2181 \
    --create --topic product --partitions 2 --replication-factor 1
bin/kafka-topics --zookeeper localhost:2181 \
    --create --topic enriched-order --partitions 3 --replication-factor 1
```

## GlobalKtable
to test, start my-kafka-spring-stream   
### prepare the data
```
curl -d 'customerId=1&name=ZhangSan' localhost:9000/customer
curl -d 'productId=1&name=Asus_VivoBook' localhost:9000/product
``` 
### Send the order
```
curl -d 'productId=1&customerId=1&orderId=1&quantity=2' localhost:9000/order
```
Now check the monitor order, the EnrichedOrder
```
EnrichedOrder(product=Product(productId=1, name=Asus_VivoBook), customer=Customer(customerId=1, name=Zhangsan), order=Order(orderId=1, customerId=1, productId=1, quantity=2))
```
If send the customer with same customerId again, the second one overwrites the first one in state store. 



