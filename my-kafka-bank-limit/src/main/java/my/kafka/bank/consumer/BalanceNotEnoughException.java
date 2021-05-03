package my.kafka.bank.consumer;

import my.kafka.bank.message.AccountBalance;
import my.kafka.bank.message.BankTransaction;

public class BalanceNotEnoughException extends RuntimeException {

    public BalanceNotEnoughException(AccountBalance balance, BankTransaction bankTransaction) {
        super(String.format("balance %s is not enough for %s", balance.toString(), bankTransaction.toString()));
    }

    public BalanceNotEnoughException(BankTransaction bankTransaction) {
        super(String.format("balance is not enough for %s", bankTransaction.toString()));
    }
}
