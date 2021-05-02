package my.kafka.bank.message;

public class AccountBalance {

    private String account;
    private Double balance;

    public AccountBalance() {
    }

    public AccountBalance(String account, Double balance) {
        this.account = account;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "AccountBalance{" +
                "account='" + account + '\'' +
                ", balance=" + balance +
                '}';
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
}
