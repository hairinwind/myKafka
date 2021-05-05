## this is the advanced project v1 of project my-kafka-bank-limit
when the balance is less than the debit amount, the tx is not allowed. It shall retry a couple of times and only be fulfilled when there is enough balance.  
In the previous project my-kafka-bank-limit, if concurrent transactions happens, the state store BALANCE has some delay, which allows some transaction to happen when the balance is not enough.  
In this project, I am going to deduct the money first, then add money. 

The BankTransaction is sent to the topic, and the status is "CREATED".
- if balance is enough, stream deduct the amount from the "from Account", set the status to be "DEBIT_SUCCESS".  
- if balance is not enough, set status to "BALANCE_NOT_ENOUGH" and send it to retry topic. 
send the BankTransaction object back to the retry topic.
- if the status is DEBIT_SUCCESS, it is sent back to the topic alpha-bank-transactions-raw to do the second step (add money)
- if the status is FULFILLED, both deduct and add steps are done, sent it to topic alpha-bank-transactions-raw-completed
- if the status is RETRY_BALANCE_NOT_ENOUGH, apply retry policy (wait and retry)

When the stream on alpha-bank-transactions-raw find the tx status is "DEBIT_SUCCESS", it can add the amount to the "to Account" and then change the status to "FULFILLED".

## create topic with 3 partitions
```
<kafka_dir>/bin/kafka-topics.sh --zookeeper 192.168.49.2:32181 \
--create \
--replication-factor 2 \
--partitions 3 \
--topic alpha-bank-transactions-raw-completed
```

## Run ClientToTestLimitConcurrent
check the detail on class in my-kafka-bank-client

## Run ClientToTestLimitConcurrentBidirection
check the detail on class in my-kafka-bank-client

## Run ClientToSendConcurrentTransactions
- run ClientToInitialAccount to have 1000 accounts in kafka
- run ClientToResetBalance to set the initial balance to 1000
- run ClientToVerifyBalance to verify all account balances are correct
- run ClientToSendConcurrentTransactions
- run ClientToVerifyBalance to verify all balances

## performance
assume there is no retry, when retry happens as retry has some wait period.  
8-10 minutes to send 495000 bank transactions.  
3 more minutes for stream to complete balance aggregation.  
The performance is on my personal laptop.

## reference
https://github.com/spring-projects/spring-kafka/blob/main/spring-kafka-docs/src/main/asciidoc/retrytopic.adoc
