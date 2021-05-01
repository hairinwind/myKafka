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
public class MyKafkaBankReceiverApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaBankReceiverApplication.class, args);
	}

	@Autowired
	private Producer producer;

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
