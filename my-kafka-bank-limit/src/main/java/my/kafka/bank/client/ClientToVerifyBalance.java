package my.kafka.bank.client;

import my.kafka.bank.message.AccountBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

import static my.kafka.bank.client.AlphaBankRestClient.allAccountBalances;

public class ClientToVerifyBalance {

    public static final Logger logger = LoggerFactory.getLogger(ClientToVerifyBalance.class);

    public static void main(String[] args) {
        List<AccountBalance> accountBalances = allAccountBalances();
        logger.info("total account number {}", accountBalances.size());

        accountBalances.stream()
//                .filter(accountBalance -> accountBalance.getBalance() != 1000)
//                .filter(accountBalance -> Integer.parseInt(accountBalance.getAccount())  < 100601)
                .sorted(new Comparator<AccountBalance>() {
                    @Override
                    public int compare(AccountBalance ab1, AccountBalance ab2) {
                        return String.CASE_INSENSITIVE_ORDER.compare(ab1.getAccount(), ab2.getAccount());
                    }
                })
                .forEach(accountBalance -> logger.info("accountBalance is not expected, {}", accountBalance));

        logger.info("verification is done");
    }
}
