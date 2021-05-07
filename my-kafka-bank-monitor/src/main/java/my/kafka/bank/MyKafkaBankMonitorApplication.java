package my.kafka.bank;

import my.kafka.bank.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MyKafkaBankMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaBankMonitorApplication.class, args);
	}

	@Autowired
	private Consumer consumer;

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
}
