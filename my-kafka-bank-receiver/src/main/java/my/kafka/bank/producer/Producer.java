package my.kafka.bank.producer;

import my.kafka.bank.Topic;
import my.kafka.bank.message.BankTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class Producer {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBankTransaction(BankTransaction bankTransaction) {
        logger.debug("send to {}, {}", Topic.TRANSACTION_RAW, bankTransaction);
        ListenableFuture<SendResult<String, Object>> sendResult = kafkaTemplate.send(Topic.TRANSACTION_RAW, bankTransaction);
        sendResult.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                                   @Override
                                   public void onSuccess(SendResult<String, Object> integerStringSendResult) {}
                                   @Override
                                   public void onFailure(Throwable throwable) {
                                        throwable.printStackTrace();
                                   }
                               }
        );
    }

}
