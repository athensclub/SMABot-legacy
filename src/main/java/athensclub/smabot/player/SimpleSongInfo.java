package athensclub.smabot.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.Serializable;
import java.util.Objects;

public class SimpleSongInfo implements Serializable {

    private String title;

    private String uri;

    private SimpleSongInfo(){}

    public SimpleSongInfo(String title, String uri) {
        this.title = title;
        this.uri = uri;
    }

     public SimpleSongInfo(AudioTrack track){
        this(track.getInfo().title, track.getInfo().uri);
     }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "SimpleSongInfo{" +
                "title='" + title + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleSongInfo)) return false;
        SimpleSongInfo that = (SimpleSongInfo) o;
        return title.equals(that.title) &&
                uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, uri);
    }
}
