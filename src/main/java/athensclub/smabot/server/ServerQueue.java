package athensclub.smabot.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandScanner;
import athensclub.smabot.player.SongList;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerQueue extends SongList<AudioTrack> {

    private final SMABotServer server;

    private final AtomicBoolean looping;

    private final AtomicInteger currentIndex;

    public ServerQueue(SMABotServer server) {
        super();
        this.server = server;
        looping = new AtomicBoolean(false);
        currentIndex = new AtomicInteger(0);
    }

    @Override
    public void shuffle() {
        AudioTrack current = currentSong();
        super.shuffle();
        swapWithFirst(current);
        currentIndex.set(0);
    }

    /**
     * Reset this queue. Does the following:
     * <ul>
     *     <li>Clear all the songs in the queue</li>
     *     <li>Set the current index to 0</li>
     *     <li>Cancel looping</li>
     * </ul>
     */
    public void reset() {
        songs.clear();
        currentIndex.set(0);
        looping.set(false);
    }

    /**
     * @return The current song in the queue.
     */
    public AudioTrack currentSong() {
        if (currentIndex.get() >= songs.size())
            return null;
        return songs.get(currentIndex.get());
    }

    /**
     * Go to the next index of this queue and return the new index, returning -1 if it reaches
     * the end of the queue.
     *
     * @return the index after incrementing.
     */
    public int nextIndex() {
        currentIndex.incrementAndGet();
        if (currentIndex.get() >= songs.size()) {
            if (looping.get())
                currentIndex.set(currentIndex.get() % songs.size());
            else
                return -1;
        }
        return currentIndex.get();
    }

    /**
     * @return The index of the current song.
     */
    public int getCurrentIndex() {
        return currentIndex.get();
    }

    /**
     * Add the song to this queue. Also make the server play the song if not already playing.
     *
     * @param song the song to add to the queue.
     */
    @Override
    public void addSong(AudioTrack song) {
        if (server.isBanned(song)) {
            AudioTrackInfo info = song.getInfo();
            throw new SMABotUserException("Can not add **" + info.title + "** (" + info.uri
                    + ") to the queue because it is banned from this server!");
        }
        super.addSong(song);
        server.playIfNotPlaying();
    }

    /**
     * @return whether this queue is currently looping.
     */
    public boolean isLooping() {
        return looping.get();
    }

    /**
     * Toggle the looping state of this queue.
     */
    public void toggleLooping() {
        boolean temp = looping.get();
        while (!looping.compareAndSet(temp, !temp))
            temp = looping.get();
    }

    /**
     * Remove ranges of song from this list. This will take the next two arguments consumed by
     * the {@link CommandScanner} given as a begin index and end index respectively. This will
     * throw {@link SMABotUserException} if there are any errors
     * (bad arguments, etc.). This will also shift the current index to match with the playing
     * song if the current song order will be shifted.
     *
     * @param sc the {@link CommandScanner} instance that will consume next two arguments to be
     *           the begin index and end index for the remove range command.
     * @return A {@link List} of all the songs that got removed.
     */
    @Override
    public List<AudioTrack> removeRange(CommandScanner sc) {
        int begin = sc.next().asInt();
        int end = sc.next().asInt();
        List<AudioTrack> removed = super.removeRange(begin, end);
        if (currentIndex.get() > begin - 1)
            currentIndex.set(currentIndex.get() - removed.size());
        return removed;
    }
}
