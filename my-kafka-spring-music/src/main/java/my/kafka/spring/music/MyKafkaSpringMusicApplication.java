package my.kafka.spring.music;

import my.kafka.spring.music.consumer.Consumer;
import my.kafka.spring.music.data.MyTopFiveSongs;
import my.kafka.spring.music.data.PlayEvent;
import my.kafka.spring.music.data.Song;
import my.kafka.spring.music.data.SongPlayCount;
import my.kafka.spring.music.data.TopFiveSongs;
import my.kafka.spring.music.producer.Producer;
import my.kafka.spring.music.producer.SongSource;
import org.apache.kafka.streams.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class MyKafkaSpringMusicApplication {

    @Autowired
    private Consumer consumer;

    @Autowired
    private Producer producer;

    public static void main(String[] args) {
        SpringApplication.run(MyKafkaSpringMusicApplication.class, args);
    }

//    @PostMapping(value = "/customer")
//    public void sendMessageToKafkaTopic(@RequestParam("customerId") String customerId,
//                                        @RequestParam("name") String customerName) {
//        Customer customer = Customer.builder()
//                .customerId(customerId)
//                .name(customerName)
//                .build();
//        this.producer.sendCustomer(customer);
//    }

    @PostMapping(value = "/initialSongs")
    public void initialSongs() {
        SongSource.songs().stream().forEach(producer::createSong);
    }

    @PostMapping(value = "/playEvent")
    public void sendPlayEvent(@RequestParam("songId") Long songId, @RequestParam("duration") Long duration) {
        PlayEvent playEvent = PlayEvent.builder().songId(songId).duration(duration).build();
        producer.sendPlayEvent(playEvent);
    }

    @GetMapping("/top5ByGenre")
    public List<TopFiveSongs> getTop5ByGenre() {
        return consumer.readTopFiveSongsByGenre();
    }

    @GetMapping("/top5")
    public List<TopFiveSongs> getTop5Songs() {
        return consumer.readTopFiveSongs();
    }

    @GetMapping("/songPlayCount")
    public List<KeyValue<Song, Long>> readSongPlayCount() {
        return consumer.readSongPlayCount();
    }

    @GetMapping("/myTopFiveSongsByGenre")
    public List<MyTopFiveSongs<SongPlayCount>> getMyTop5ByGenre() {
        return consumer.readMyTopFiveSongsByGenre();
    }

}
