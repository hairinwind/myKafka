package my.kafka.spring.music.data;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
public class MyTopFiveSongs<T extends Comparable> {

    private List<T> songs = new ArrayList<>();

    public void add(final T t) {
        songs.add(t);
        Collections.sort(songs);
        if (songs.size() > 5) {
            songs = songs.subList(0, 5);
        }
    }

    public void remove(final T t) {
        songs.remove(t);
    }

    //test serialize and deserialize
    public static void main(String[] args) {
        MyTopFiveSongs<SongPlayCount> myTopFiveSongs = new MyTopFiveSongs<>();
        myTopFiveSongs.setSongs(Arrays.asList(SongPlayCount.builder().songId(1L).plays(1).build()));
        System.out.println(myTopFiveSongs);

        Serde<MyTopFiveSongs<SongPlayCount>> myTopFiveSerde = new JsonSerde<>(new TypeReference<MyTopFiveSongs<SongPlayCount>>(){} );
        byte[] bytes = myTopFiveSerde.serializer().serialize("testTopic", myTopFiveSongs);
        MyTopFiveSongs<SongPlayCount> myTopFiveSongsDes = myTopFiveSerde.deserializer().deserialize("testTopic", bytes);
        System.out.println(myTopFiveSongsDes);
        System.out.println(myTopFiveSongs.equals(myTopFiveSongsDes));
    }
}
