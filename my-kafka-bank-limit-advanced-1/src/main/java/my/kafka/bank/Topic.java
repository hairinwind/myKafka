package my.kafka.bank;

public class Topic {

    // this is the topic to accept bank transactions
    public static final String TRANSACTION_RAW = "alpha-bank-transactions-raw";

    // the topic for retry messages
    public static final String TRANSACTION_RAW_RETRY = "alpha-bank-transactions-raw-retry";

    // the topic for completed messages
    public static final String TRANSACTION_RAW_COMPLETED = "alpha-bank-transactions-raw-completed";
}
