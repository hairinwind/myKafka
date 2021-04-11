
## Basic idea
send a transaction message to topic  
```
key: target_account
value: amount
```
sum the amount (balance) and save to local state store

read balance from local state store

## url to test
create a tx, the POST request below is to deposit $1 into account 100001
```
curl -X POST localhost:8080/account/100001?amount=1
```
read balance
```
curl -X GET localhost:8080/account/100001
```

## deploy another instance 
I deployed another instance on kubernetes. see kube-deploy.yaml  ```curl -X GET 192.168.49.2:31880/account/100001```
Only one instance can read the local state store.  
The other one would get the exception 
```
org.apache.kafka.streams.errors.InvalidStateStoreException: The state store, balance, may have migrated to another instance.
```
turn off one instance, the other one would work automatically.
