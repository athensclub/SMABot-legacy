package athensclub.smabot.data.provider.impl.onmem;

import athensclub.smabot.SMABotUtil;
import athensclub.smabot.command.Permission;
import athensclub.smabot.data.provider.ServerProvider;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * An implementation of {@link ServerProvider} using java's own {@link java.util.Map} to store the data.
 */
public class OnMemoryServerProvider extends OnMemoryProvider<OnMemoryServerProvider.ServerData> implements ServerProvider, Serializable {

    protected static class JailInfoData implements Serializable {
        public String channel, endTime;

        public JailInfoData(String channel, String endTime) {
            this.channel = channel;
            this.endTime = endTime;
        }
    }

    /**
     * A representation of internal data. should not be used outside of this package.
     */
    protected static class ServerData implements Serializable {

        public String prefix;

        public final AtomicBoolean announce;

        public final ConcurrentMap<String, String> banned;

        public final ConcurrentMap<String, Boolean> listening;

        public final ConcurrentMap<String, ConcurrentMap<String, Boolean>> permissions;

        public final ConcurrentMap<String, JailInfoData> jailed;

        public ServerData() {
            announce = new AtomicBoolean(true);
            banned = new ConcurrentHashMap<>();
            listening = new ConcurrentHashMap<>();
            permissions = new ConcurrentHashMap<>();
            jailed = new ConcurrentHashMap<>();
        }

        @Override
        public String toString() {
            return "ServerData{" +
                    "prefix='" + prefix + '\'' +
                    ", announce=" + announce +
                    ", banned=" + banned +
                    ", permissions=" + permissions +
                    ", listening=" + listening +
                    ", jailed=" + jailed +
                    '}';
        }

    }

    @Override
    public void ban(String server, String uri, String title) {
        ServerData data = getData(server, ServerData::new);
        data.banned.put(uri, title);
    }

    @Override
    public void unban(String server, String uri) {
        ServerData data = getData(server, ServerData::new);
        data.banned.remove(uri);
    }

    @Override
    public boolean isBanned(String server, String uri) {
        ServerData data = getData(server, ServerData::new);
        return data.banned.containsKey(uri);
    }

    @Override
    public Map<String, String> getAllBannedSongs(String server) {
        ServerData data = getData(server, ServerData::new);
        return Collections.unmodifiableMap(data.banned);
    }

    @Override
    public Optional<Boolean> hasPermissionOptional(String server, String user, Permission perm) {
        ServerData data = getData(server, ServerData::new);
        ConcurrentMap<String, Boolean> mapping = SMABotUtil.getOrCreateAtomic(data.permissions, user, ConcurrentHashMap::new);
        return Optional.ofNullable(mapping.get(perm.getName()));
    }


    @Override
    public void setPermission(String server, String user, Permission perm, boolean value) {
        ServerData data = getData(server, ServerData::new);
        ConcurrentMap<String, Boolean> mapping = SMABotUtil.getOrCreateAtomic(data.permissions, user, ConcurrentHashMap::new);
        mapping.put(perm.getName(), value);
    }

    @Override
    public boolean isAnnounce(String server) {
        ServerData data = getData(server, ServerData::new);
        return data.announce.get();
    }

    @Override
    public void toggleAnnounce(String server) {
        ServerData data = getData(server, ServerData::new);
        boolean temp = data.announce.get();
        while (!data.announce.compareAndSet(temp, !temp))
            temp = data.announce.get();
    }

    @Override
    public Optional<String> getPrefix(String server) {
        ServerData data = getData(server, ServerData::new);
        return Optional.ofNullable(data.prefix);
    }

    @Override
    public void setPrefix(String server, String prefix) {
        ServerData data = getData(server, ServerData::new);
        data.prefix = prefix;
    }

    @Override
    public boolean isListeningTo(String server, String textChannel) {
        ServerData data = getData(server, ServerData::new);
        return data.listening.getOrDefault(textChannel, true);
    }

    @Override
    public void setListeningTo(String server, String textChannel, boolean listening) {
        ServerData data = getData(server, ServerData::new);
        data.listening.put(textChannel, listening);
    }

    @Override
    public void jail(String server, String user, String channel, String endTime) {
        ServerData data = getData(server, ServerData::new);
        data.jailed.put(user, new JailInfoData(channel, endTime));
    }

    @Override
    public void unjail(String server, String user) {
        ServerData data = getData(server, ServerData::new);
        data.jailed.remove(user);
    }

    @Override
    public Map<String, Map.Entry<String, String>> getAllJailed(String server) {
        ServerData data = getData(server, ServerData::new);
        return data.jailed.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> Map.entry(e.getValue().channel, e.getValue().endTime)));
    }

    @Override
    public Map<String, Map<String, Map.Entry<String, String>>> getAllJailed() {
        HashMap<String, Map<String, Map.Entry<String, String>>> result = new HashMap<>();
        get().forEach((server, data) -> {
            data.jailed.forEach((user, info) -> {
                if (!result.containsKey(user))
                    result.put(user, new HashMap<>());
                result.get(user).put(server, Map.entry(info.channel, info.endTime));
            });
        });
        return result;
    }


    @Override
    public Optional<String> jailChannel(String server, String user) {
        ServerData data = getData(server, ServerData::new);
        return Optional.ofNullable(data.jailed.get(user)).map(o -> o.channel);
    }

    @Override
    public Optional<String> jailEndTime(String server, String user) {
        ServerData data = getData(server, ServerData::new);
        return Optional.ofNullable(data.jailed.get(user)).map(o -> o.endTime);
    }

}
