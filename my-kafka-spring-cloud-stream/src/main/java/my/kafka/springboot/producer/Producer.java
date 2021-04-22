package my.kafka.springboot.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(Source.class)
public class Producer {

    @Autowired
    private MessageChannel output;

    public void send(String text) {
        output.send(MessageBuilder.withPayload(text).build());
    }

}
