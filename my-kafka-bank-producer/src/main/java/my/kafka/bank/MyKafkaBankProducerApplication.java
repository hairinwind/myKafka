package my.kafka.bank;

import my.kafka.bank.message.BankTransaction;
import my.kafka.bank.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MyKafkaBankProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaBankProducerApplication.class, args);
	}

	@Autowired
	Producer producer;

	@PostMapping("/send")
	public String send(@RequestParam String fromAccount, @RequestParam String toAccount, @RequestParam Double amount) {
		BankTransaction bankTransaction = new BankTransaction(fromAccount, toAccount, amount);
		producer.sendBankTransaction(bankTransaction);
		return "SUCCESS";
	}
}
