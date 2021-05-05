package my.kafka.bank.message;

import java.util.Arrays;
import java.util.List;

/**
 * The BankTransactionInternal is created from BankTransaction
 * one bank transaction is to split into two
 * for example: BankTransaction is send $1 from accountA to accountB
 * it shall split into two BankTransactionInternal
 * on accountA -1
 * on accountB +1
 */
public class BankTransactionInternal {

    private String txId; // the bankTransaction id
    private String account;
    private Double amount;

    public static List<BankTransactionInternal> splitBankTransaction(BankTransaction bankTransaction) {
        BankTransactionInternal txOnFromAccount = new BankTransactionInternal(
                bankTransaction.getTxId(),
                bankTransaction.getFromAccount(),
                bankTransaction.getAmount() * -1
        );
        BankTransactionInternal txOnToAccount = new BankTransactionInternal(
                bankTransaction.getTxId(),
                bankTransaction.getToAccount(),
                bankTransaction.getAmount()
        );
        return Arrays.asList(txOnFromAccount, txOnToAccount);
    }

    public BankTransactionInternal() {
    }

    public BankTransactionInternal(String txId, String account, Double amount) {
        this.txId = txId;
        this.account = account;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "BankTransactionInternal{" +
                "txId='" + txId + '\'' +
                ", account='" + account + '\'' +
                ", amount=" + amount +
                '}';
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

}
