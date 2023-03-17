package athensclub.smabot.command.handler.server;

import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

public class AnnounceCommandHandler implements CommandHandler<ServerCommandData> {

    @Override
    public void handle(ServerCommandData data) {
        data.getSMABotServer().toggleAnnounce();
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Announce")
                        .setDescription(data.getSMABotServer().isAnnounce() ?
                                "I will now display the currently playing song!" :
                                "I will now not display the currently playing song!")
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }

}
