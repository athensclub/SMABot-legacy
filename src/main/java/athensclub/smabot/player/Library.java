package athensclub.smabot.player;

import athensclub.smabot.command.CommandScanner;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Library extends SongList<SimpleSongInfo> implements Serializable {

    protected final AtomicBoolean isPrivate;

    public Library() {
        isPrivate = new AtomicBoolean(false);
    }

    /**
     * @return An immutable {@link Library} backed by the value of this library.
     */
    public Library immutable() {
        if (this instanceof Immutable)
            return this;
        return new Immutable(this);
    }

    public void setPrivate(boolean is) {
        isPrivate.set(is);
    }

    public boolean isPrivate() {
        return isPrivate.get();
    }

    @Override
    public String toString() {
        return "Library{" +
                "isPrivate=" + isPrivate +
                ", songs=" + songs +
                '}';
    }

    public static class Immutable extends Library {

        public Immutable(Library from) {
            isPrivate.set(from.isPrivate());
            songs.addAll(from.songs);
        }

        @Override
        public void setPrivate(boolean is) {
            throw new UnsupportedOperationException("This library is immutable: " + this);
        }

        @Override
        public void addSong(SimpleSongInfo song) {
            throw new UnsupportedOperationException("This library is immutable: " + this);
        }

        @Override
        public void shuffle() {
            throw new UnsupportedOperationException("This library is immutable: " + this);
        }

        @Override
        public List<SimpleSongInfo> removeRange(int begin, int end) {
            throw new UnsupportedOperationException("This library is immutable: " + this);
        }

        @Override
        public List<SimpleSongInfo> removeRange(CommandScanner sc) {
            throw new UnsupportedOperationException("This library is immutable: " + this);
        }
    }

}
