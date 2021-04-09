package my.kafka.partition;

import my.kafka.partition.consumer.Consumer;
import my.kafka.partition.producer.Producer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaPartitionProjectApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(KafkaPartitionProjectApplication.class, args);
	}

	@Autowired
	private Producer producer;

	@Autowired
	private Consumer consumer;

	@Override
	public void run(String... args) throws Exception {
		if (args.length ==0 && StringUtils.isBlank(args[0])) {
			throw new IllegalArgumentException("Either 'sent' or 'consumer' shall be provided as argument");
		}

		if (args[0].equalsIgnoreCase("send"))  {
			System.out.println("send message");
			producer.sendMessages();
		} else {
			System.out.println("consume message");
			consumer.start();
		}
	}
}
