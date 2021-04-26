package my.kafka.bank;

import my.kafka.bank.consumer.Consumer;
import my.kafka.bank.message.AccountBalance;
import my.kafka.bank.message.BankTransaction;
import my.kafka.bank.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MyKafkaBankApplication {

	@Autowired
	private Producer producer;

	@Autowired
	private Consumer consumer;

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaBankApplication.class, args);
	}

	@GetMapping("/testSend")
	public String testSend() {
		BankTransaction bankTransaction = new BankTransaction("100001", "100002", 1D);
		producer.sendBankTransaction(bankTransaction);
		return "SUCCESS";
	}

	@GetMapping("/testSendFromExternal")
	public String testSendFormExternal() {
		BankTransaction bankTransaction = new BankTransaction("external", "100002", 1D);
		producer.sendBankTransaction(bankTransaction);
		return "SUCCESS";
	}

	@GetMapping("/account/{accountNumber}")
	public ResponseEntity<AccountBalance> getAccountBalance(@PathVariable String accountNumber) {
		return ResponseEntity.ok(consumer.getBalance(accountNumber));
	}
}
