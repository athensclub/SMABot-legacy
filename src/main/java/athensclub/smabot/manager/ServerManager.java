package athensclub.smabot.manager;

import athensclub.smabot.SMABotUtil;
import athensclub.smabot.data.provider.ServerProvider;
import athensclub.smabot.server.SMABotServer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.javacord.api.DiscordApi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A manager that handle server.
 */
public class ServerManager {

    private ConcurrentMap<String, SMABotServer> servers;

    private final AudioPlayerManager audioManager;

    private final PermissionManager permissionManager;

    private final ServerProvider serverProvider;

    private final DiscordApi api;

    public ServerManager(AudioPlayerManager manager, PermissionManager permissionManager,
                         ServerProvider serverProvider, DiscordApi api) {
        servers = new ConcurrentHashMap<>();
        audioManager = manager;
        this.permissionManager = permissionManager;
        this.serverProvider = serverProvider;
        this.api = api;
    }

    /**
     * Get a {@link SMABotServer} instance from the guild id.
     *
     * @param id the guild id to get {@link SMABotServer} instance from.
     * @return a {@link SMABotServer} instance from the guild id.
     */
    public SMABotServer getServer(String id) {
        return SMABotUtil.getOrCreateAtomic(servers, id,
                () -> new SMABotServer(api, audioManager, permissionManager, id, serverProvider));
    }

    @Override
    public String toString() {
        return "ServerManager{" +
                "servers=" + servers +
                '}';
    }
}
