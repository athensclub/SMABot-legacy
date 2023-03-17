package athensclub.smabot.data.provider.impl.mongo;

import athensclub.smabot.data.provider.UserProvider;
import athensclub.smabot.player.Library;
import athensclub.smabot.player.SimpleSongInfo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static athensclub.smabot.data.provider.impl.mongo.MongoUtil.UPSERT_OPTION;
import static athensclub.smabot.data.provider.impl.mongo.MongoUtil.snowflakeEq;
import static com.mongodb.client.model.Updates.*;

/**
 * An implementation of {@link athensclub.smabot.data.provider.UserProvider} using MongoDB.
 */
public class MongoUserProvider implements UserProvider {

    private final MongoCollection<Document> collection;

    public MongoUserProvider(MongoDatabase database) {
        collection = database.getCollection("users");
    }

    @Override
    public int libraryCount(String id) {
        ensureUser(id);
        return Objects.requireNonNull(collection.find(snowflakeEq(id)).first()).getList("libraries", Document.class).size();
    }

    @Override
    public List<SimpleSongInfo> libraryRemoveRange(String id, int index, int begin, int end) {
        // i don't know a way to remove range in mongodb, so i will just read into java, update
        // in java and write back to mongodb.
        ensureUser(id);
        Library library = toLibrary(Objects.requireNonNull(collection.find(snowflakeEq(id)).first())
                .getList("libraries", Document.class)
                .get(index - 1));
        List<SimpleSongInfo> result = library.removeRange(begin, end);
        collection.updateOne(snowflakeEq(id),
                set(String.join(".", "libraries", Integer.toString(index - 1), "songs"),
                        library.getSongs().stream().map(this::toDocument).collect(Collectors.toList())));
        return result;
    }

    @Override
    public void libraryAddSong(String id, int index, SimpleSongInfo song) {
        ensureUser(id);
        collection.updateOne(snowflakeEq(id),
                push(String.join(".", "libraries", Integer.toString(index - 1), "songs"),
                        toDocument(song)));
    }

    @Override
    public Library libraryGet(String id, int index) {
        ensureUser(id);
        return toLibrary(Objects.requireNonNull(collection.find(snowflakeEq(id)).first())
                .getList("libraries", Document.class)
                .get(index - 1));
    }

    @Override
    public void libraryNew(String id) {
        ensureUser(id);
        collection.updateOne(snowflakeEq(id), push("libraries", new Document()
                .append("private", false)
                .append("songs", Collections.emptyList())));
    }

    @Override
    public List<Library> libraryGetAll(String id) {
        ensureUser(id);
        return Objects.requireNonNull(collection.find(snowflakeEq(id)).first())
                .getList("libraries", Document.class)
                .stream()
                .map(lib -> toLibrary(lib).immutable())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void librarySetVisibility(String id, int index, boolean isPrivate) {
        ensureUser(id);
        collection.updateOne(snowflakeEq(id),
                set(String.join(".", "libraries", Integer.toString(index - 1), "private"), isPrivate));
    }

    /**
     * Convert {@link SimpleSongInfo} instance into {@link Document} instance.
     *
     * @param info the {@link SimpleSongInfo} instance to convert.
     * @return the converted {@link Document} instance.
     */
    private Document toDocument(SimpleSongInfo info) {
        return new Document()
                .append("title", info.getTitle())
                .append("uri", info.getUri());
    }

    /**
     * Create a {@link SimpleSongInfo} instance from a given {@link Document} which contains data
     * about the song info.
     *
     * @param info the {@link Document} instance that contains the song info.
     * @return the {@link SimpleSongInfo} instance.
     */
    private SimpleSongInfo toSimpleSongInfo(Document info) {
        return new SimpleSongInfo(info.getString("title"), info.getString("uri"));
    }

    /**
     * Create a {@link Library} instance from a given {@link Document} which contains data about
     * the library.
     *
     * @param lib a {@link Document} instance that contains data about the library.
     * @return a {@link Library} instance
     */
    private Library toLibrary(Document lib) {
        Library result = new Library();
        result.setPrivate(lib.getBoolean("private"));
        lib.getList("songs", Document.class)
                .forEach(s -> result.addSong(toSimpleSongInfo(s)));
        return result;
    }

    private void ensureUser(String id) {
        collection.updateOne(snowflakeEq(id),
                setOnInsert(new Document()
                        .append("snowflake", id)
                        .append("libraries", Collections.emptyList())), UPSERT_OPTION);
    }
}
