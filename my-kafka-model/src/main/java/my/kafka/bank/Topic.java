package my.kafka.bank;

public class Topic {

    // this is the topic to accept bank transactions
    public static final String TRANSACTION_RAW = "alpha-bank-transactions-raw";

    // this is the topic for bank transactions internal which is split from bank transactions
    public static final String TRANSACTION_INTERNAL = "alpha-bank-transactions-internal";

    // this is the topic to retry bank transactions
    public static final String TRANSACTION_RAW_RETRY = "alpha-bank-transactions-raw-retry";

    // this is the topic for completed transaction
    public static final String TRANSACTION_RAW_COMPLETED = "alpha-bank-transactions-raw-completed";

    // this is the dead letter of TRANSACTION_RAW_RETRY
    public static final String TRANSACTION_RAW_RETRY_DLT = "alpha-bank-transactions-raw-retry-dlt";

    //topics for my-kafka-spring-stream
    // GlobalKTable
    public static final String ORDER = "order";
    public static final String CUSTOMER = "customer";
    public static final String PRODUCT = "product";
    public static final String ENRICHED_ORDER = "enriched-order";
    // PageViewRegionLambdaExample
    public static final String PAGE_VIEWS = "PageViews";
    public static final String USER_PROFILES = "UserProfiles";
    public static final String PAGE_VIEWS_BY_REGION = "PageViewsByRegion";

}
