package athensclub.smabot.player;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandScanner;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongList<T> implements Serializable {

    protected final List<T> songs;

    public SongList() {
        songs = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Add the song into this list.
     *
     * @param song the song to add.
     */
    public void addSong(T song) {
        songs.add(song);
    }

    /**
     * @return The unmodifiable version of the list of songs.
     */
    public List<T> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    /**
     * Swap the given track, which must be in this list, with the first track in list.
     *
     * @param track the track to swap with the first track in this list..
     */
    public void swapWithFirst(AudioTrack track) {
        Collections.swap(songs, songs.indexOf(track), 0);
    }

    /**
     * Shuffle the order of the songs in this list.
     */
    public void shuffle() {
        Collections.shuffle(songs);
    }

    /**
     * Remove ranges of song from this list. This will throw {@link SMABotUserException} when
     * it fails to do so.
     *
     * @param begin the begin index of the songs to be removed (1-indexed, inclusive).
     * @param end   the end index of the songs to be removed (1-indexed, inclusive).
     * @return A {@link List} of all the songs that got removed.
     */
    public List<T> removeRange(int begin, int end) {
        if (begin > end)
            throw new SMABotUserException("Bad input (begin index > end index). Please try again.");
        if (begin - 1 < 0 || begin - 1 >= songs.size() || end - 1 < 0 || end - 1 >= songs.size())
            throw new SMABotUserException("Input index out of range. Please try again.");
        List<T> removed = new ArrayList<>();
        for (int i = begin - 1; i <= end - 1; i++)
            removed.add(songs.remove(begin - 1));
        return removed;
    }

    /**
     * Remove ranges of song from this list. This will take the next two arguments consumed by
     * the {@link CommandScanner} given as a begin index and end index respectively. This will
     * throw {@link SMABotUserException} if there are any errors
     * (bad arguments, etc.).
     *
     * @param sc the {@link CommandScanner} instance that will consume next two arguments to be
     *           the begin index and end index for the remove range command.
     * @return A {@link List} of all the songs that got removed.
     */
    public List<T> removeRange(CommandScanner sc) {
        int begin = sc.next().asInt();
        int end = sc.next().asInt();
        return removeRange(begin, end);
    }
}
