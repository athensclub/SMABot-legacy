package athensclub.smabot.command.handler.server;

import athensclub.smabot.Main;
import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class PrefixCommandHandler implements CommandHandler<ServerCommandData> {

    @Override
    public void handle(ServerCommandData data) {
        if (data.getCommandBody().isBlank()) {
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Prefix")
                            .setDescription("The current prefix for this server is (without the single quotes): '" +
                                    data.getServerProvider().getPrefix(data.getServer().getIdAsString()).orElse(Main.BUILD_MODE.prefix) + "'")
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }

        //discord will automatically trim the message so trailing space is impossible without escape characters
        StringBuilder prefix = new StringBuilder();
        List<Integer> raw = data.getCommandBody()
                .codePoints()
                .boxed()
                .collect(Collectors.toList());
        boolean escaped = false;
        for (int current : raw) {
            if (escaped) {
                switch (current) {
                    case 's' -> prefix.append(' ');
                    case '\\' -> prefix.append('\\');
                    default -> throw new SMABotUserException("Unknown escape character: " +
                            (new StringBuilder().appendCodePoint(current).toString()));
                }
                escaped = false;
                continue;
            }

            if (current == '\\') {
                escaped = true;
                continue;
            }

            prefix.appendCodePoint(current);
        }
        if (escaped)
            throw new SMABotUserException("Unexpected character '\\\\' at the end of prefix.");

        data.getServerProvider().setPrefix(data.getServer().getIdAsString(), prefix.toString());
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Prefix")
                        .setDescription("Successfully set the prefix for this bot in this server to be" +
                                " (without the single quotes): '" + prefix + "'")
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }

}
