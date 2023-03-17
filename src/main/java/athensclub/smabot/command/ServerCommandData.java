package athensclub.smabot.command;

import athensclub.smabot.data.provider.DataProvider;
import athensclub.smabot.server.SMABotServer;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

/**
 * Extended {@link CommandData} for server commands.
 */
public class ServerCommandData extends CommandData{

    private final Server server;

    private final ServerVoiceChannel voiceChannel;

    public ServerCommandData(Command cmd, String body, MessageCreateEvent event, SMABotServer smaBotServer, DataProvider provider, DiscordApi api) {
        super(cmd, body, event, smaBotServer, provider, api);
        server = event.getServer().get(); // server null is handled by MessageHandler?
        voiceChannel = getUser().getConnectedVoiceChannel(server).orElse(null);
    }

    public Server getServer() {
        return server;
    }

    public ServerVoiceChannel getVoiceChannel() {
        return voiceChannel;
    }
}
