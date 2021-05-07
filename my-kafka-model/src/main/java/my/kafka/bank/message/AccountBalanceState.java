package my.kafka.bank.message;

import java.time.Instant;

public class AccountBalanceState {

    private String account;
    private Double balance;
    private Double previousBalance;
    private BankTransaction bankTransaction;
    private Instant instant;

    public AccountBalanceState() {}

    public AccountBalanceState(String account, Double balance, Double previousBalance, BankTransaction bankTransaction) {
        this.account = account;
        this.balance = balance;
        this.previousBalance = previousBalance;
        this.bankTransaction = bankTransaction;
        this.instant = Instant.now();
    }

    public void add(BankTransaction bankTransaction) {
        this.bankTransaction = bankTransaction;
        this.previousBalance = this.balance;
        this.balance = this.previousBalance + bankTransaction.getAmount();
        this.instant = Instant.now();
    }

    public void minus(BankTransaction bankTransaction) {
        this.bankTransaction = bankTransaction;
        this.previousBalance = this.balance;
        this.balance = this.previousBalance - bankTransaction.getAmount();
        this.instant = Instant.now();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(Double previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BankTransaction getBankTransaction() {
        return bankTransaction;
    }

    public void setBankTransaction(BankTransaction bankTransaction) {
        this.bankTransaction = bankTransaction;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public String toString() {
        return "AccountBalanceState{" +
                "account='" + account + '\'' +
                ", balance=" + balance +
                ", previousBalance=" + previousBalance +
                ", bankTransaction=" + bankTransaction +
                ", instant=" + instant +
                '}';
    }
}
