package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.Optional;

public class ListenCommandHandler implements CommandHandler<ServerCommandData> {
    @Override
    public void handle(ServerCommandData data) {
        String channelID = data.getCommandBody();
        if (channelID.isBlank()) {
            StringBuilder message = new StringBuilder();
            message.append("I am listening to the following channels:\n");
            for (ServerTextChannel channel : data.getServer().getTextChannels()) {
                message.append(data.getServerProvider().isListeningTo(data.getServer().getIdAsString(), channel.getIdAsString()) ? ":white_check_mark:" : ":x:")
                        .append(' ')
                        .append(channel.getName())
                        .append("\n");
            }
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle("Listening Channels")
                            .setDescription(message.toString()))
                    .send(data.getTextChannel());
        } else {
            Optional<ServerTextChannel> channelOptional = data.getServer().getTextChannelById(channelID);
            if (channelOptional.isEmpty())
                throw new SMABotUserException("There is no text channel with id: " + channelID);
            data.getServerProvider()
                    .setListeningTo(data.getServer().getIdAsString(), channelOptional.get().getIdAsString(), true);
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Channel Listen")
                            .setDescription("This bot will now be listening to the text channel **" + channelOptional.get().getName() + "**.")
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
        }
    }
}
