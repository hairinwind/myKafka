package my.kafka.spring.music.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import my.kafka.spring.music.data.MyTopFiveSongs;
import my.kafka.spring.music.data.PlayEvent;
import my.kafka.spring.music.data.Song;
import my.kafka.spring.music.data.SongPlayCount;
import my.kafka.spring.music.data.TopFiveSerde;
import my.kafka.spring.music.data.TopFiveSongs;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import static my.kafka.spring.music.Constants.MIN_CHARTABLE_DURATION;
import static my.kafka.spring.music.Constants.TOP_FIVE_KEY;
import static my.kafka.spring.music.StateStore.ALL_SONGS;
import static my.kafka.spring.music.StateStore.MY_TOP_FIVE_SONGS_BY_GENRE_STORE;
import static my.kafka.spring.music.StateStore.SONG_PLAY_COUNT_STORE;
import static my.kafka.spring.music.StateStore.TOP_FIVE_SONGS_BY_GENRE_STORE;
import static my.kafka.spring.music.StateStore.TOP_FIVE_SONGS_STORE;
import static my.kafka.spring.music.Topic.PLAY_EVENTS;
import static my.kafka.spring.music.Topic.SONG_FEED;

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
public class KafkaConsumerConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KTable<Song, Long> streamsTopology(StreamsBuilder builder) {
        JsonSerde<PlayEvent> playEventSerde = new JsonSerde<>(PlayEvent.class);
        final KStream<String, PlayEvent> playEvents = builder.stream(
                PLAY_EVENTS,
                Consumed.with(Serdes.String(), playEventSerde));

        Serde<Song> valueSongSerde = new JsonSerde<>(Song.class);
        final KTable<Long, Song>
                songTable =
                builder.table(SONG_FEED,
                        Materialized.<Long, Song, KeyValueStore<Bytes, byte[]>>as(ALL_SONGS)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(valueSongSerde));

        final KStream<Long, PlayEvent> playsBySongId =
                playEvents.filter((region, event) -> event.getDuration() >= MIN_CHARTABLE_DURATION)
                        // repartition based on song id
                        .map((key, value) -> KeyValue.pair(value.getSongId(), value));

        final KStream<Long, Song> songPlays = playsBySongId.leftJoin(songTable,
                (value1, song) -> song,
                Joined.with(Serdes.Long(), playEventSerde, valueSongSerde));

        songPlays.peek((k, v) -> logger.info("songPlays -> {} : {}", k, v));

        Serde<Song> keySongSerde = new JsonSerde<>(Song.class);
        final KTable<Song, Long> songPlayCounts = songPlays.groupBy(
                (songId, song) -> song,
                Grouped.with(keySongSerde, valueSongSerde)
        ).count(Materialized.<Song, Long, KeyValueStore<Bytes, byte[]>>as(SONG_PLAY_COUNT_STORE)
                .withKeySerde(valueSongSerde)
                .withValueSerde(Serdes.Long()));

        songPlayCounts.toStream().peek((k, v) -> logger.info("songPlayCounts -> {} : {}", k, v));

        return songPlayCounts;
    }

    @Bean
    public KTable<String, TopFiveSongs> groupByGenre(KTable<Song, Long> songPlayCounts) {
        Serde<SongPlayCount> songPlayCountSerde = new JsonSerde<>(SongPlayCount.class);
        Serde<TopFiveSongs> topFiveSerde = new TopFiveSerde();

        KTable<String, TopFiveSongs> groupByGenre = songPlayCounts.groupBy(
                (song, plays) -> KeyValue.pair(song.getGenre().toLowerCase(),
                        new SongPlayCount(song.getId(), plays)),
                Grouped.with(Serdes.String(), songPlayCountSerde))
                // aggregate into a TopFiveSongs instance that will keep track
                // of the current top five for each genre. The data will be available in the
                // top-five-songs-genre store
                /*
                 * this is the aggregate on GroupedKTable (songPlayCounts is a KTable, and groupBy above)
                 * the first aggregator function is to add new item to aggregate result
                 * for example, it adds { key: 19, value: {Song(id=19, album=24K Magic, artist=Bruno Mars, name=That's What I Like, genre=Pop) : 1} }
                 * this object is send to aggregate.add()
                 * the second aggregator function is to remove old item with same key
                 * when the second playEvent of the same song arrives
                 * the SongPlayCount is {Song(id=19, album=24K Magic, artist=Bruno Mars, name=That's What I Like, genre=Pop) : 2}
                 * That would be added into aggregate by first aggregator
                 * and the previous result {Song(id=19, album=24K Magic, artist=Bruno Mars, name=That's What I Like, genre=Pop) : 1} is removed by second aggregator
                 *
                 * the second aggregator is only invoked when there is old item (same key) existing
                 * the second aggregator is invoked before the first aggregator
                 */
                .aggregate(TopFiveSongs::new,
                        (aggKey, value, aggregate) -> {
                            logger.info("...first aggregator {} {} {}", aggKey, value, aggregate);
                            aggregate.add(value);
                            return aggregate;
                        },
                        (aggKey, value, aggregate) -> {
                            logger.info("...second aggregator {} {} {}", aggKey, value, aggregate);
                            aggregate.remove(value);
                            return aggregate;
                        },
                        Materialized.<String, TopFiveSongs, KeyValueStore<Bytes, byte[]>>as(TOP_FIVE_SONGS_BY_GENRE_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(topFiveSerde)
                );
        return groupByGenre;
    }

    @Bean
    public KTable<String, TopFiveSongs> groupByAll(KTable<Song, Long> songPlayCounts) {
        Serde<SongPlayCount> songPlayCountSerde = new JsonSerde<>(SongPlayCount.class);
        Serde<TopFiveSongs> topFiveSerde = new TopFiveSerde();
        KTable<String, TopFiveSongs> groupByAll = songPlayCounts.groupBy((song, plays) ->
                        KeyValue.pair(TOP_FIVE_KEY, new SongPlayCount(song.getId(), plays)),
                Grouped.with(Serdes.String(), songPlayCountSerde))
                .aggregate(TopFiveSongs::new,
                        (aggKey, value, aggregate) -> {
                            aggregate.add(value);
                            return aggregate;
                        },
                        (aggKey, value, aggregate) -> {
                            aggregate.remove(value);
                            return aggregate;
                        },
                        Materialized.<String, TopFiveSongs, KeyValueStore<Bytes, byte[]>>as(TOP_FIVE_SONGS_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(topFiveSerde)
                );

        return groupByAll;
    }

    // my implementation of groupByGenre
    // Make MyTopFiveSongs support generic, MyTopFiveSongs<SongPlayCount>
    // then I can use JsonSerde and it does not depend TopFiveSerde
    @Bean
    public KTable<String, MyTopFiveSongs<SongPlayCount>> myGroupByGenreTop5(KTable<Song, Long> songPlayCounts) {
        Serde<SongPlayCount> songPlayCountSerde = new JsonSerde<>(SongPlayCount.class);
        Serde<MyTopFiveSongs<SongPlayCount>> myTopFiveSerde = new JsonSerde<>(new TypeReference<MyTopFiveSongs<SongPlayCount>>(){} );

        KTable<String, MyTopFiveSongs<SongPlayCount>> groupByGenre = songPlayCounts.groupBy(
                (song, plays) -> KeyValue.pair(song.getGenre().toLowerCase(),
                        new SongPlayCount(song.getId(), plays)),
                Grouped.with(Serdes.String(), songPlayCountSerde))
                .aggregate(() -> new MyTopFiveSongs<SongPlayCount>(),
                        (aggKey, value, aggregate) -> {
                            logger.info("...myTopFive adder {} {} {}", aggKey, value, aggregate);
                            aggregate.add(value);
                            return aggregate;
                        },
                        (aggKey, value, aggregate) -> {
                            logger.info("...myTopFive subtractor {} {} {}", aggKey, value, aggregate);
                            aggregate.remove(value);
                            return aggregate;
                        },
                        Materialized.<String, MyTopFiveSongs<SongPlayCount>, KeyValueStore<Bytes, byte[]>>as(MY_TOP_FIVE_SONGS_BY_GENRE_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(myTopFiveSerde)
                );

        return groupByGenre;
    }

}
