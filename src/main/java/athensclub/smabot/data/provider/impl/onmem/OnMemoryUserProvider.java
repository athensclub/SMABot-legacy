package athensclub.smabot.data.provider.impl.onmem;

import athensclub.smabot.data.provider.UserProvider;
import athensclub.smabot.player.Library;
import athensclub.smabot.player.SimpleSongInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of {@link UserProvider} using java's plain {@link List} to handle data.
 */
public class OnMemoryUserProvider extends OnMemoryProvider<OnMemoryUserProvider.UserData> implements UserProvider, Serializable {

    protected static class UserData implements Serializable {

        public final List<Library> libraries;

        public UserData() {
            libraries = Collections.synchronizedList(new ArrayList<>());
        }

        @Override
        public String toString() {
            return "UserData{" +
                    "libraries=" + libraries +
                    '}';
        }
    }

    @Override
    public int libraryCount(String id) {
        UserData data = getData(id, UserData::new);
        return data.libraries.size();
    }

    @Override
    public List<SimpleSongInfo> libraryRemoveRange(String id, int index, int begin, int end) {
        UserData data = getData(id, UserData::new);
        return data.libraries.get(index - 1).removeRange(begin, end);
    }

    @Override
    public void libraryAddSong(String id, int index, SimpleSongInfo song) {
        UserData data = getData(id, UserData::new);
        data.libraries.get(index - 1).addSong(song);
    }

    @Override
    public Library libraryGet(String id, int index) {
        UserData data = getData(id, UserData::new);
        return data.libraries.get(index - 1);
    }

    @Override
    public void libraryNew(String id) {
        UserData data = getData(id, UserData::new);
        data.libraries.add(new Library());
    }

    @Override
    public List<Library> libraryGetAll(String id) {
        UserData data = getData(id, UserData::new);
        return data.libraries
                .stream()
                .map(Library::immutable)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void librarySetVisibility(String id, int index, boolean isPrivate) {
        UserData data = getData(id, UserData::new);
        data.libraries
                .get(index-1)
                .setPrivate(isPrivate);
    }
}
