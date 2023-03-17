package athensclub.smabot.command.handler.server;

import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

public class ShuffleCommandHandler implements CommandHandler<ServerCommandData> {

    @Override
    public void handle(ServerCommandData data) {
        data.getSMABotServer().getQueue().shuffle();
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Shuffle")
                        .setDescription("Successfully shuffled the queue!")
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }

}
