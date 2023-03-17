package athensclub.smabot.handler;

import athensclub.smabot.data.provider.DataProvider;
import athensclub.smabot.manager.ServerManager;
import athensclub.smabot.server.SMABotServer;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;

;import java.util.Optional;

public class VoiceStateHandler implements ServerVoiceChannelMemberJoinListener, ServerVoiceChannelMemberLeaveListener {

    private final ServerManager serverManager;

    private final DataProvider dataProvider;

    private final DiscordApi api;

    public VoiceStateHandler(ServerManager serverManager, DataProvider dataProvider, DiscordApi api) {
        this.serverManager = serverManager;
        this.dataProvider = dataProvider;
        this.api = api;
    }

    private void updateIfNeeded(ServerVoiceChannel channel) {
        SMABotServer server = serverManager.getServer(channel.getServer().getIdAsString());
        if (server.getVoiceChannel() != null &&
                channel.getId() == server.getVoiceChannel().getId())
            server.updateVoiceState();
    }

    private void jailIfNeeded(ServerVoiceChannel channel, User user) {
        Optional<String> jailID = dataProvider.getServerProvider()
                .jailChannel(channel.getServer().getIdAsString(), user.getIdAsString());
        if(jailID.isPresent() && !jailID.get().equals(channel.getIdAsString()))
            user.move(api.getServerVoiceChannelById(jailID.get()).get());
    }

    @Override
    public void onServerVoiceChannelMemberJoin(ServerVoiceChannelMemberJoinEvent event) {
        updateIfNeeded(event.getChannel());
        jailIfNeeded(event.getChannel(), event.getUser());
    }

    @Override
    public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {
        updateIfNeeded(event.getChannel());
    }
}
