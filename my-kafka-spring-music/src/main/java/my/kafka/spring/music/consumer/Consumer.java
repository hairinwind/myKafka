package my.kafka.spring.music.consumer;

import my.kafka.spring.music.StateStore;
import my.kafka.spring.music.Topic;
import my.kafka.spring.music.data.MyTopFiveSongs;
import my.kafka.spring.music.data.PlayEvent;
import my.kafka.spring.music.data.Song;
import my.kafka.spring.music.data.SongPlayCount;
import my.kafka.spring.music.data.TopFiveSongs;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class Consumer {
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    StreamsBuilderFactoryBean defaultKafkaStreamsBuilder;

    @Autowired
    StreamsBuilder streamsBuilder;

    @PostConstruct
    public void postConstruct() {
        //print topology
        Topology topology = streamsBuilder.build();
        logger.info("topology: {}", topology.describe());
    }

    @KafkaListener(topics = Topic.SONG_FEED)
    public void consumeSongs(Song song) {
        logger.info("song -> {}", song);
    }

    @KafkaListener(topics = Topic.PLAY_EVENTS)
    public void consumePlayEvent(PlayEvent playEvent) {
        logger.info("playEvent -> {}", playEvent);
    }

    public List<TopFiveSongs> readTopFiveSongsByGenre() {
        List<TopFiveSongs> topFiveSongs = new ArrayList<>();
        ReadOnlyKeyValueStore<String, TopFiveSongs> store = defaultKafkaStreamsBuilder.getKafkaStreams().store(
                StateStore.TOP_FIVE_SONGS_BY_GENRE_STORE,
                QueryableStoreTypes.keyValueStore());
        store.all().forEachRemaining(record -> topFiveSongs.add(record.value));
        return topFiveSongs;
    }

    public List<TopFiveSongs> readTopFiveSongs() {
        List<TopFiveSongs> topFiveSongs = new ArrayList<>();
        ReadOnlyKeyValueStore<String, TopFiveSongs> store = defaultKafkaStreamsBuilder.getKafkaStreams().store(
                StateStore.TOP_FIVE_SONGS_STORE,
                QueryableStoreTypes.keyValueStore());
        store.all().forEachRemaining(record -> topFiveSongs.add(record.value));
        return topFiveSongs;
    }

    public List<KeyValue<Song, Long>> readSongPlayCount() {
        List<KeyValue<Song, Long>> result = new ArrayList<>();
        ReadOnlyKeyValueStore<Song, Long> store = defaultKafkaStreamsBuilder.getKafkaStreams().store(
                StateStore.SONG_PLAY_COUNT_STORE,
                QueryableStoreTypes.keyValueStore());
        store.all().forEachRemaining(record -> result.add(record));
        return result;
    }

    public List<MyTopFiveSongs<SongPlayCount>> readMyTopFiveSongsByGenre() {
        List<MyTopFiveSongs<SongPlayCount>> topFiveSongs = new ArrayList<>();
        ReadOnlyKeyValueStore<String, MyTopFiveSongs<SongPlayCount>> store = defaultKafkaStreamsBuilder.getKafkaStreams().store(
                StateStore.MY_TOP_FIVE_SONGS_BY_GENRE_STORE,
                QueryableStoreTypes.keyValueStore());
        store.all().forEachRemaining(record -> topFiveSongs.add(record.value));
        return topFiveSongs;
    }
}
