package my.kafka.bank.client;

import my.kafka.bank.message.AccountBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static my.kafka.bank.client.AlphaBankRestClient.allAccountBalances;

public class ClientToResetBalance {
    public static final Logger logger = LoggerFactory.getLogger(ClientToResetBalance.class);

//    public static final

    public static void main(String[] args) {
        Double targetBalance = 1000D;
        resetAllBalances(targetBalance);
        logger.info("done!");
    }

    public static void resetAllBalances(Double targetBalance) {
        List<AccountBalance> accountBalances = allAccountBalances();
        accountBalances.stream()
                .filter(accountBalance -> !accountBalance.getBalance().equals(targetBalance))
                .forEach(accountBalance -> resetBalance(accountBalance, targetBalance));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        accountBalances = allAccountBalances();
        accountBalances.stream()
                .forEach(accountBalance -> logger.info("accountBalance is {}", accountBalance));
    }

    public static void resetBalance(AccountBalance accountBalance, Double targetBalance) {
        String fromAccount = "external";
        String toAccount = "external";
        Double amount = Math.abs(targetBalance - accountBalance.getBalance());
        if (targetBalance < accountBalance.getBalance()) {
            fromAccount = accountBalance.getAccount();
        } else {
            toAccount = accountBalance.getAccount();
        }

        logger.info("accountBalance {}", accountBalance);
        AlphaBankRestClient.moveMoney(fromAccount, toAccount, amount);
    }



}
