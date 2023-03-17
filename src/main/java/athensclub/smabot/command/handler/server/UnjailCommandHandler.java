package athensclub.smabot.command.handler.server;

import athensclub.smabot.command.CommandScanner;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;

public class UnjailCommandHandler implements CommandHandler<ServerCommandData> {
    @Override
    public void handle(ServerCommandData data) {
        CommandScanner sc = data.scanner();
        User target = sc.next().asUser();
        data.getServerProvider().unjail(data.getServer().getIdAsString(), target.getIdAsString());
        new MessageBuilder().setEmbed(new EmbedBuilder()
                .setTitle("Unjail")
                .setDescription("Successfully unjailed " + target.getNicknameMentionTag() + "!")
                .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }
}
