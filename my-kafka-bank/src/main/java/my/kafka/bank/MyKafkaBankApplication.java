package my.kafka.bank;

import my.kafka.bank.consumer.Consumer;
import my.kafka.bank.message.AccountBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class MyKafkaBankApplication {

	@Autowired
	private Consumer consumer;

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaBankApplication.class, args);
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
