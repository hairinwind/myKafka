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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class
MyKafkaBankLimitAdvancedApplication {

	@Autowired
	private Producer producer;

	@Autowired
	private Consumer consumer;

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaBankLimitAdvancedApplication.class, args);
	}

	@PostMapping("/send")
	public String send(@RequestParam String fromAccount, @RequestParam String toAccount, @RequestParam Double amount) {
		BankTransaction bankTransaction = new BankTransaction(fromAccount, toAccount, amount);
		producer.sendBankTransaction(bankTransaction);
		return "SUCCESS";
	}

	@GetMapping("/account/{accountNumber}")
	public ResponseEntity<AccountBalance> getAccountBalance(@PathVariable String accountNumber) {
		return ResponseEntity.ok(consumer.getBalance(accountNumber));
	}

	@GetMapping("/fetchAllLocalBalances")
	public ResponseEntity<List<AccountBalance>> fetchAllLocalBalances() {
		List<AccountBalance> body = consumer.fetchAllLocalBalances();
		return ResponseEntity.ok(body);
	}
}
