package my.kafka.spring.music.producer;

import my.kafka.spring.music.data.Song;

import java.util.ArrayList;
import java.util.List;

public class SongSource {

    private static List<Song> songs = new ArrayList<>();

    static {
        songs.add(new Song(1L, "Fresh Fruit For Rotting Vegetables", "Dead Kennedys", "Chemical Warfare", "Punk"));
        songs.add(new Song(2L, "We Are the League", "Anti-Nowhere League", "Animal", "Punk"));
        songs.add(new Song(3L, "Live In A Dive", "Subhumans", "All Gone Dead", "Punk"));
        songs.add(new Song(4L, "PSI", "Wheres The Pope?", "Fear Of God", "Punk"));
        songs.add(new Song(5L, "Totally Exploited", "The Exploited", "Punks Not Dead", "Punk"));
        songs.add(new Song(6L, "The Audacity Of Hype", "Jello Biafra And The Guantanamo School Of Medicine", "Three Strikes", "Punk"));
        songs.add(new Song(7L, "Licensed to Ill", "The Beastie Boys", "Fight For Your Right", "Hip Hop"));
        songs.add(new Song(8L, "De La Soul Is Dead", "De La Soul", "Oodles Of O's", "Hip Hop"));
        songs.add(new Song(9L, "Straight Outta Compton", "N.W.A", "Gangsta Gangsta", "Hip Hop"));
        songs.add(new Song(10L, "Fear Of A Black Planet", "Public Enemy", "911 Is A Joke", "Hip Hop"));
        songs.add(new Song(11L, "Curtain Call - The Hits", "Eminem", "Fack", "Hip Hop"));
        songs.add(new Song(12L, "21", "Adele", "Rolling in the Deep", "Pop"));
        songs.add(new Song(13L, "In The Lonely Hour", "Sam Smith", "Stay With Me", "Pop"));
        songs.add(new Song(14L, "The Calling", "Hilltop Hoods", "The Calling", "Hip Hop"));
        songs.add(new Song(15L, "x", "Ed Sheeran", "Thinking Out Loud", "Pop"));
        songs.add(new Song(16L, "V", "Maroon 5", "Sugar", "Pop"));
        songs.add(new Song(17L, "This Is What The Truth Feels Like", "Gwen Stefani", "Red Flag", "Pop"));
        songs.add(new Song(18L, "This Is Acting", "Sia", "Alive", "Pop"));
        songs.add(new Song(19L, "24K Magic", "Bruno Mars", "That's What I Like", "Pop"));
        songs.add(new Song(20L, "Black Sunday", "Cypress Hill", "Insane in the Brain", "Hip Hop"));
        songs.add(new Song(21L, "Aquemini", "Outkast", "Aquemini", "Hip Hop"));
    }

    public static List<Song> songs() {
        return songs;
    }

}
