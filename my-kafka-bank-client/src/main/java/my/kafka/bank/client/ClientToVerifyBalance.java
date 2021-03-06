package my.kafka.bank.client;

import my.kafka.bank.Constants;
import my.kafka.bank.message.AccountBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

import static my.kafka.bank.client.AlphaBankRestClient.allAccountBalances;

public class ClientToVerifyBalance {

    public static final Logger logger = LoggerFactory.getLogger(ClientToVerifyBalance.class);

    public static void main(String[] args) {
//        verify(101000);
//        verify(100100);
        verify(100002);
    }

    public static void verify(int maxAccountNumber) {
        List<AccountBalance> accountBalances = allAccountBalances();
        logger.info("total account number {}", accountBalances.size());

        accountBalances.stream()
                .filter(accountBalance -> !Constants.EXTERNAL_ACCOUNT.equalsIgnoreCase(accountBalance.getAccount()))
                .filter(accountBalance -> Integer.parseInt(accountBalance.getAccount())  <= maxAccountNumber)
                .filter(accountBalance -> {
                    int accountIndex = Integer.parseInt(accountBalance.getAccount()) - 100000;
                    return accountBalance.getBalance().doubleValue() != accountIndex * 2 -1;
                })
                .sorted(new Comparator<AccountBalance>() {
                    @Override
                    public int compare(AccountBalance ab1, AccountBalance ab2) {
                        return String.CASE_INSENSITIVE_ORDER.compare(ab1.getAccount(), ab2.getAccount());
                    }
                })
                .forEach(accountBalance -> logger.info(accountBalance.toString()));


        logger.info("verification is done");
    }
}
