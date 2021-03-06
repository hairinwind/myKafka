package my.kafka.spring.music.data;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class TopFiveSongs implements Iterable<SongPlayCount>, Serializable {

    private final Map<Long, SongPlayCount> currentSongs = new HashMap<>();

    private final TreeSet<SongPlayCount> topFive = new TreeSet<>((o1, o2) -> {
        final Long o1Plays = o1.getPlays();
        final Long o2Plays = o2.getPlays();

        final int result = o2Plays.compareTo(o1Plays);
        if (result != 0) {
            return result;
        }
        final Long o1SongId = o1.getSongId();
        final Long o2SongId = o2.getSongId();
        return o1SongId.compareTo(o2SongId);
    });

    @Override
    public String toString() {
        return currentSongs.toString();
    }

    public void add(final SongPlayCount songPlayCount) {
        if (currentSongs.containsKey(songPlayCount.getSongId())) {
            topFive.remove(currentSongs.remove(songPlayCount.getSongId()));
        }
        topFive.add(songPlayCount);
        currentSongs.put(songPlayCount.getSongId(), songPlayCount);
        if (topFive.size() > 5) {
            final SongPlayCount last = topFive.last();
            currentSongs.remove(last.getSongId());
            topFive.remove(last);
        }
    }

    public void remove(final SongPlayCount value) {
        topFive.remove(value);
        currentSongs.remove(value.getSongId());
    }

    @Override
    public Iterator<SongPlayCount> iterator() {
        return topFive.iterator();
    }

    // The code here is to test JsonSerde
    // The JsonSerde does not work with TopFiveSongs
    public static void main(String[] args) {
        TopFiveSongs tp5 = new TopFiveSongs();
        tp5.add(SongPlayCount.builder().songId(1L).plays(1000L).build());
        System.out.println(tp5);

//        Serde<TopFiveSongs> topFiveSerde = new JsonSerde<>(TopFiveSongs.class);
        final TopFiveSerde topFiveSerde = new TopFiveSerde();

        byte[] testTopics = topFiveSerde.serializer().serialize("testTopic", tp5);
        TopFiveSongs tp5Des = topFiveSerde.deserializer().deserialize("testTopic", testTopics);
        System.out.println(tp5Des);

        // test map JsonSerde
        Map<String, List<SongPlayCount>> songPlaysByGenre = new HashMap<>();
        songPlaysByGenre.put("testGenre", Arrays.asList(SongPlayCount.builder().songId(1L).plays(1000L).build()));
        System.out.println(songPlaysByGenre);

        // Serde<Map<String, List<SongPlayCount>>> mapSerde = new JsonSerde<>(Map.class); // this does not work
        Serde<Map<String, List<SongPlayCount>>> mapSerde = new JsonSerde<>(
                new TypeReference<Map<String, List<SongPlayCount>>>(){} /* create TypeReference this way to let deserializer know the type of the object in List*/
        );
        byte[] bytes = mapSerde.serializer().serialize("testTopic", songPlaysByGenre);
        Map songPlaysByGenreDes = mapSerde.deserializer().deserialize("testTopic", bytes);
        System.out.println(songPlaysByGenreDes);
        System.out.println(songPlaysByGenreDes.equals(songPlaysByGenre));
        List genreList = (List)songPlaysByGenreDes.get("testGenre");
        System.out.println(genreList.get(0).getClass().getName()); //after deserialize, it is linkedHashMap
        // java object to json is not always bi-direction convertible
        // like the one above, java object is a map {testGenre=[SongPlayCount(songId=1, plays=1000)]}
        // convert it to json, it is { testGenre: [ {songId: 1, plays: 1000} ] }
        // when java try to convert the JSON string above back to Java Object,
        // it does not know the type of {songId: 1, plays: 1000} is SongPlayCount. The JSON string does not have that info.
        // so, the detail TypeReference has to be provided here: new TypeReference<Map<String, List<SongPlayCount>>>(){}
    }
}
