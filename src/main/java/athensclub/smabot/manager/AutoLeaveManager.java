package athensclub.smabot.manager;

import athensclub.smabot.server.SMABotServer;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AutoLeaveManager {

    private final SMABotServer server;

    private CompletableFuture<Void> leaveFuture;

    public AutoLeaveManager(SMABotServer server) {
        this.server = server;
    }

    /**
     * Called to make the bot update its data to be up to date with this server's voice channel
     * that this bot is in. This will be called within {@link SMABotServer#updateVoiceState()}.
     */
    public void updateVoiceState() {
        int nonBot = (int) server.getVoiceChannel()
                .getConnectedUsers()
                .stream()
                .filter(u -> !u.isBot())
                .count();
        if (nonBot == 0) {
            if (leaveFuture == null) {
                leaveFuture = CompletableFuture.runAsync(() -> {
                    new MessageBuilder()
                            .setEmbed(new EmbedBuilder()
                                    .setTitle("Disconnect")
                                    .setDescription("I am disconnecting from voice channel because I was inactive for too long!")
                                    .setColor(Color.GREEN))
                            .send(server.getTextChannel());
                    server.disconnect();
                }, CompletableFuture.delayedExecutor(10,TimeUnit.MINUTES));
            }
        } else if (leaveFuture != null) {
            leaveFuture.cancel(true);
            leaveFuture = null;
        }

    }

}
