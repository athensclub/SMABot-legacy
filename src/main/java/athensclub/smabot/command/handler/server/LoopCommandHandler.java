package athensclub.smabot.command.handler.server;

import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

/**
 * Handle looping command.
 */
public class LoopCommandHandler implements CommandHandler<ServerCommandData> {

    @Override
    public void handle(ServerCommandData data) {
        data.getSMABotServer().getQueue().toggleLooping();
        boolean loop = data.getSMABotServer().getQueue().isLooping();
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Loop")
                        .setDescription(loop ? "Currently looping the queue!" :
                                "The queue is now not looping!")
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }

}
