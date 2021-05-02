package my.kafka.bank;

public class Topic {

    // this is the topic to accept bank transactions
    public static final String TRANSACTION_RAW = "alpha-bank-transactions-raw";

    // this is the topic for bank transactions internal which is split from bank transactions
    public static final String TRANSACTION_INTERNAL = "alpha-bank-transactions-internal";

    public static final String TRANSACTION_RAW_RETRY = "alpha-bank-transactions-raw-retry";
}
