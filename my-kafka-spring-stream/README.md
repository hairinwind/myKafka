## my-kafka-spring-stream
this is practising the sample code from https://github.com/confluentinc/kafka-streams-examples

## Topics 
Topics for GlobalKTablesExample
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
topics for PageViewRegionLambdaExample
```
bin/kafka-topics --zookeeper localhost:2181 \
     --create --topic PageViews --partitions 1 --replication-factor 1
bin/kafka-topics --zookeeper localhost:2181 \
     --create --topic UserProfiles --partitions 1 --replication-factor 1
bin/kafka-topics --zookeeper localhost:2181 \
     --create --topic PageViewsByRegion --partitions 1 --replication-factor 1
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

##
### prepare the data
```
curl -d "userId=Jason&region=Asia" localhost:9000/userProfile
curl -d "userId=ZhangSan&region=Asia" localhost:9000/userProfile
curl -d "userId=Chris&region=NorthAmerica" localhost:9000/userProfile
```
### send the PageView Data
```
curl -X POST localhost:9000/sendPageViewTestData
```

