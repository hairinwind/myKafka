
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
