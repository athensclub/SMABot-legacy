package athensclub.smabot.data.provider.impl.mongo;

import athensclub.smabot.command.Permission;
import athensclub.smabot.data.provider.ServerProvider;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

import static athensclub.smabot.data.provider.impl.mongo.MongoUtil.UPSERT_OPTION;
import static athensclub.smabot.data.provider.impl.mongo.MongoUtil.snowflakeEq;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

/**
 * An implementation of {@link ServerProvider} using MongoDB.
 */
public class MongoServerProvider implements ServerProvider {

    private final MongoCollection<Document> collection;

    public MongoServerProvider(MongoDatabase database) {
        collection = database.getCollection("servers");
    }

    @Override
    public void ban(String server, String uri, String title) {
        ensureServer(server);
        collection.updateOne(snowflakeEq(server),
                set(String.join(".", "banned", uri), title));
    }

    @Override
    public void unban(String server, String uri) {
        ensureServer(server);
        collection.updateOne(snowflakeEq(server),
                unset(String.join(".", "banned", uri)));
    }

    @Override
    public boolean isBanned(String server, String uri) {
        ensureServer(server);
        return collection.find(and(snowflakeEq(server),
                exists(String.join(".", "banned", uri)))).first() != null;
    }

    @Override
    public Map<String, String> getAllBannedSongs(String server) {
        ensureServer(server);
        Map<String, String> result = new HashMap<>();
        Objects.requireNonNull(collection.find(snowflakeEq(server)).first())
                .get("banned", Document.class)
                .forEach((str, obj) -> result.put(str, obj.toString()));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Optional<Boolean> hasPermissionOptional(String server, String user, Permission perm) {
        ensureServer(server);
        return Optional.ofNullable(Objects.requireNonNull(collection.find(snowflakeEq(server)).first())
                .getEmbedded(List.of("permissions", user, perm.getName()), Boolean.class));
    }

    @Override
    public void setPermission(String server, String user, Permission perm, boolean value) {
        ensureServer(server);
        collection.updateOne(snowflakeEq(server),
                set(String.join(".", List.of("permissions", user, perm.getName())), value));
    }

    @Override
    public boolean isAnnounce(String server) {
        ensureServer(server);
        return Objects.requireNonNull(collection.find(snowflakeEq(server)).first())
                .getBoolean("announce");
    }

    @Override
    public void toggleAnnounce(String server) {
        ensureServer(server);
        boolean announce = Objects.requireNonNull(collection.find(snowflakeEq(server)).first())
                .getBoolean("announce");
        collection.updateOne(snowflakeEq(server), set("announce", !announce));
    }

    @Override
    public Optional<String> getPrefix(String server) {
        ensureServer(server);
        String prefix = Objects.requireNonNull(collection.find(snowflakeEq(server)).first())
                .getString("prefix");
        return Optional.ofNullable(prefix);
    }

    @Override
    public void setPrefix(String server, String prefix) {
        ensureServer(server);
        collection.updateOne(snowflakeEq(server), set("prefix", prefix));
    }

    @Override
    public boolean isListeningTo(String server, String textChannel) {
        ensureServer(server);
        Boolean result = Objects.requireNonNull(collection.find(snowflakeEq(server)).first())
                .getEmbedded(List.of("listening", textChannel), Boolean.class);
        return result == null ? true : result; // default value is true.
    }

    @Override
    public void setListeningTo(String server, String textChannel, boolean listening) {
        ensureServer(server);
        collection.updateOne(snowflakeEq(server),
                set(String.join(".", "listening", textChannel), listening));
    }

    @Override
    public void jail(String server, String user, String channel, String endTime) {
        ensureServer(server);
        collection.updateOne(snowflakeEq(server),
                set(String.join(".", "jailed", user),
                        Map.of("channel", channel, "endTime", endTime)));
    }

    @Override
    public void unjail(String server, String user) {
        ensureServer(server);
        collection.updateOne(snowflakeEq(server),
                unset(String.join(".", "jailed", user)));
    }

    @Override
    public Map<String, Map.Entry<String, String>> getAllJailed(String server) {
        ensureServer(server);
        HashMap<String, Map.Entry<String, String>> result = new HashMap<>();
        Objects.requireNonNull(collection.find(snowflakeEq(server)).first())
                .get("jailed", Document.class)
                .forEach((user, info) -> {
                    Document infoDoc = (Document) info;
                    result.put(user, Map.entry(infoDoc.getString("channel"), infoDoc.getString("endTime")));
                });
        return result;
    }

    @Override
    public Map<String, Map<String, Map.Entry<String, String>>> getAllJailed() {
        HashMap<String, Map<String, Map.Entry<String, String>>> result = new HashMap<>();
        collection.find(not(size("jailed", 0))).forEach(doc -> {
            doc.get("jailed", Document.class)
                    .forEach((user, infoObj) -> {
                        if (!result.containsKey(user))
                            result.put(user, new HashMap<>());
                        Document info = (Document) infoObj;
                        result.get(user).put(doc.getString("snowflake"), Map.entry(info.getString("channel"), info.getString("endTime")));
                    });
        });
        return result;
    }

    @Override
    public Optional<String> jailChannel(String server, String user) {
        ensureServer(server);
        return Optional.ofNullable(Objects.requireNonNull(collection.find(snowflakeEq(server)).first())
                .get("jailed", Document.class)
                .get(user))
                .map(o -> ((Document) o).getString("channel"));
    }

    @Override
    public Optional<String> jailEndTime(String server, String user) {
        ensureServer(server);
        return Optional.ofNullable(Objects.requireNonNull(collection.find(snowflakeEq(server)).first())
                .get("jailed", Document.class)
                .get(user))
                .map(o -> ((Document) o).getString("endTime"));
    }


    private void ensureServer(String id) {
        collection.updateOne(snowflakeEq(id),
                setOnInsert(new Document()
                        .append("snowflake", id)
                        .append("prefix", null)
                        .append("announce", true)
                        .append("banned", Collections.emptyMap())
                        .append("jailed", Collections.emptyMap())
                        .append("listening", Collections.emptyMap())
                        .append("permissions", Collections.emptyMap())), UPSERT_OPTION);
    }
}
