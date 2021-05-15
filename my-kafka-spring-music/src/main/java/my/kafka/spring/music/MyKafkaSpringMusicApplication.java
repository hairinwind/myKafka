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
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class MyKafkaSpringMusicApplication {

    public static final Logger logger = LoggerFactory.getLogger(MyKafkaSpringMusicApplication.class);

    @Autowired
    private Consumer consumer;

    @Autowired
    private Producer producer;

    public static void main(String[] args) {
        SpringApplication.run(MyKafkaSpringMusicApplication.class, args);
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        StreamsBuilder streamsBuilder = event.getApplicationContext().getBean(StreamsBuilder.class);
        Topology topology = streamsBuilder.build();
        logger.info("topology: {}", topology.describe());
    }

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

    @GetMapping("/allSongs")
    public List<Song> getAllSongs() {
        return consumer.readAllSongs();
    }

}
