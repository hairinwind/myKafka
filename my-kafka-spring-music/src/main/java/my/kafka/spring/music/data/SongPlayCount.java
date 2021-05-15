package my.kafka.spring.music.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongPlayCount implements Comparable<SongPlayCount> {

    private Long songId;
    private long plays;

    @Override
    public int compareTo(SongPlayCount another) {
        if (another == null) {
            return -1;
        }
        return Long.compare(plays, another.getPlays());
    }
}
