package my.kafka.springboot;

import my.kafka.springboot.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MyKafkaSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaSpringbootApplication.class, args);
	}

	@Autowired
	private Producer producer;

	@PostMapping("/send")
	public String send(@RequestBody String message) {
		producer.send(message);
		return "Success";
	}

	@GetMapping("/test")
	public String test() {
		return "test";
	}

}
