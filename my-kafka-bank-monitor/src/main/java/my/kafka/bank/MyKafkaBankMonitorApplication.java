package my.kafka.bank;

import my.kafka.bank.consumer.Consumer;
import my.kafka.bank.message.BankTransaction;
import my.kafka.bank.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class MyKafkaBankMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaBankMonitorApplication.class, args);
	}

	@Autowired
	private Consumer consumer;

	@Autowired
	private Producer producer;

	@GetMapping("/txCount")
	public ResponseEntity<String> txCount() {
		String returnText = consumer.getTransactionRawCount() + " | " + consumer.getTransactionInternalCount();
		return ResponseEntity.ok(returnText);
	}

	@PostMapping("/resetCount")
	public ResponseEntity<String> resetCount() {
		consumer.resetCount();
		return txCount();
	}

	@PostMapping("/send")
	public String send(@RequestParam String fromAccount, @RequestParam String toAccount, @RequestParam Double amount) {
		BankTransaction bankTransaction = new BankTransaction(fromAccount, toAccount, amount);
		producer.sendBankTransaction(bankTransaction);
		return "SUCCESS";
	}

	@PostMapping("/testSend")
	public String testSend() {
		BankTransaction bankTransaction = new BankTransaction("100001", "100002", 1D);
		producer.sendBankTransaction(bankTransaction);
		return "SUCCESS";
	}

	@PostMapping("/testSendFromExternal")
	public String testSendFormExternal() {
		BankTransaction bankTransaction = new BankTransaction("external", "100002", 1D);
		producer.sendBankTransaction(bankTransaction);
		return "SUCCESS";
	}
}
