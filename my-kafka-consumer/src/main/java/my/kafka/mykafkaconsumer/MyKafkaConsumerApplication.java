package my.kafka.mykafkaconsumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.UnknownHostException;
import java.sql.SQLOutput;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

@SpringBootApplication
public class MyKafkaConsumerApplication implements CommandLineRunner {

	private static final String TEST_TOPIC = "test001";
	public static final String BOOTSTRAP_SERVERS = "192.168.49.2:31090,192.168.49.2:31091,192.168.49.2:31092";

	public static void main(String[] args) {
		SpringApplication.run(MyKafkaConsumerApplication.class, args);
	}

	public Properties kafkaConfig() throws UnknownHostException {
		Properties config = new Properties();
		//client.id is the logical name in kafka log
		// config.put("client.id", InetAddress.getLocalHost().getHostName());
		//group.id is to load balance the consumers.
		config.put("group.id", "testConsumer001");
		config.put("bootstrap.servers", BOOTSTRAP_SERVERS);
		config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		// config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		return config;
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("start consuming messages");
		int count = 0;
		try (final KafkaConsumer<String, Long> consumer = new KafkaConsumer<>(kafkaConfig())) {
			consumer.subscribe(Collections.singleton(TEST_TOPIC));

			while (true) {
				final ConsumerRecords<String, Long> consumerRecords = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
				for (final ConsumerRecord<String, Long> consumerRecord : consumerRecords) {
					System.out.println(consumerRecord);
					// If there is no error, manually commit the offset
					// this works when autocommit is disabled in config
					// config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
					// consumer.commitSync();
					count ++;
				}
				// code for demo purpose, in real world, it shall keep running
				if (count >= 3) {
					break;
				} else {
					Thread.sleep(500);
				}
			}
		}
		System.out.println("stop consuming messages");
	}
}
