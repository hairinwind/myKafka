package my.kafka.bank;

public class Constants {

    public static final String EXTERNAL_ACCOUNT = "external";

    public static boolean isExternalAccount(String account) {
        return EXTERNAL_ACCOUNT.equalsIgnoreCase(account);
    }

}
