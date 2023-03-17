package athensclub.smabot.command.handler.server;

import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.List;

/**
 * Responsible for handling command rr (remove range).
 */
public class RRCommandHandler implements CommandHandler<ServerCommandData> {

    @Override
    public void handle(ServerCommandData data) {
        List<AudioTrack> removed = data.getSMABotServer().getQueue().removeRange(data.scanner());
        if (removed.size() > 0)
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Remove")
                            .setDescription("Removed " + removed.size() + " songs!")
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
        else
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Remove")
                            .setDescription("I did not remove any songs!")
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
    }

}
