package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.Optional;

public class NoListenCommandHandler implements CommandHandler<ServerCommandData> {
    @Override
    public void handle(ServerCommandData data) {
        String channelID = data.getCommandBody();
        if (channelID.isBlank())
            throw new SMABotUserException("Please enter text channel id.");

        Optional<ServerTextChannel> channelOptional = data.getServer().getTextChannelById(channelID);
        if (channelOptional.isEmpty())
            throw new SMABotUserException("There is no text channel with id: " + channelID);
        data.getServerProvider()
                .setListeningTo(data.getServer().getIdAsString(), channelOptional.get().getIdAsString(), false);
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Channel Listen")
                        .setDescription("This bot will now not listen to the text channel **" + channelOptional.get().getName() + "**.")
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());

    }
}
