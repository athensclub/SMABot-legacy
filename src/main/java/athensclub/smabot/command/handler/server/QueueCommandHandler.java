package athensclub.smabot.command.handler.server;

import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.List;

/**
 * A command to display all the song in the queue.
 */
public class QueueCommandHandler implements CommandHandler<ServerCommandData> {

    @Override
    public void handle(ServerCommandData data) {
        List<AudioTrack> queue = data.getSMABotServer().getQueue().getSongs();
        int currentIndex = data.getSMABotServer().getQueue().getCurrentIndex();
        StringBuilder msg = new StringBuilder();
        msg.append("Songs in queue: ");
        if (!queue.isEmpty()) {
            msg.append('\n');
            for (int i = 0; i < queue.size(); i++) {
                if (i == currentIndex)
                    msg.append("**:arrow_down: current track**\n");
                AudioTrackInfo info = queue.get(i).getInfo();
                msg.append(i + 1)
                        .append(". **")
                        .append(info.title)
                        .append("** (")
                        .append(info.uri)
                        .append(")\n");
                if (i == currentIndex)
                    msg.append("**:arrow_up: current track**\n");
            }
        } else
            msg.append("None");
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Queue")
                        .setDescription(msg.toString())
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }
}
