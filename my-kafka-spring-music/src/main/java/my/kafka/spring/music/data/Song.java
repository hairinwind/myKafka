package my.kafka.spring.music.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Song {

    private Long id;
    private String album;
    private String artist;
    private String name;
    private String genre;

}
