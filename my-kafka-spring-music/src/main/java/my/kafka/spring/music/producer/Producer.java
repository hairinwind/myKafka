package my.kafka.spring.music.producer;

import my.kafka.spring.music.Topic;
import my.kafka.spring.music.data.PlayEvent;
import my.kafka.spring.music.data.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private KafkaTemplate<Long, Object> kafkaTemplate;

//    public void sendCustomer(Customer customer) {
//        this.kafkaTemplate.send(Topic.CUSTOMER, customer.getCustomerId(), customer);
//    }
//
//    public void sendOrder(Order order) {
//        this.kafkaTemplate.send(Topic.ORDER, order.getOrderId(), order);
//    }
//
//    public void sendProduct(Product product) {
//        this.kafkaTemplate.send(Topic.PRODUCT, product.getProductId(), product);
//    }
//
//    public void sendUserProfile(UserProfile userProfile) {
//        this.kafkaTemplate.send(Topic.USER_PROFILES, userProfile.getUserId(), userProfile);
//    }
//
//    public void sendPageView(PageView pageView) {
//        this.kafkaTemplate.send(Topic.PAGE_VIEWS, pageView);
//    }

    public void createSong(Song song) {
        this.kafkaTemplate.send(Topic.SONG_FEED, song.getId(), song);
    }

    public void sendPlayEvent(PlayEvent playEvent) {
        this.kafkaTemplate.send(Topic.PLAY_EVENTS, playEvent.getSongId(), playEvent);
    }
}
