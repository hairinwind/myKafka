package my.kafka.mykafkaproducer;

import my.kafka.mykafkaproducer.service.KafkaProducerService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@SpringBootApplication
public class MyKafkaProducerApplication implements CommandLineRunner {

	private static Logger LOG = LoggerFactory.getLogger(MyKafkaProducerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaProducerApplication.class, args);
	}

	@Autowired
	private KafkaProducerService kafkaProducerService;

	@Override
	public void run(String... args) {
		LOG.info("EXECUTING : command line runner " + args);

		if(args == null || args.length == 0) {
			kafkaProducerService.sendTestMessage();
		}
	}

	@Bean("kafkaStringProducer")
	public KafkaProducer kafkaProducer() {
		Properties config = new Properties();
		config.put("bootstrap.servers", "192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092");
		config.put("acks", "all");
		config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return new KafkaProducer<String, String>(config);
	}

}
