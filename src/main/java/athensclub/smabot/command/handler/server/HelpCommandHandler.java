package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandScanner;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import com.google.gson.Gson;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HelpCommandHandler implements CommandHandler<ServerCommandData> {

    private String summary;

    private Map<String, String> commands;

    private static class HelpJsonData implements Serializable {

        public List<String> summary;
        public Map<String, List<String>> commands;

    }

    public HelpCommandHandler() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("help.json")) {
            Gson gson = new Gson();
            HelpJsonData data = gson.fromJson(new InputStreamReader(Objects.requireNonNull(in)), HelpJsonData.class);
            summary = String.join("\n", data.summary);
            commands = new HashMap<>();
            data.commands.forEach((name, info) -> commands.put(name, String.join("\n", info)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(ServerCommandData data) {
        CommandScanner sc = data.scanner();
        if (!sc.hasNext()) {
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Help")
                            .setDescription(summary)
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }

        String inputCommand = sc.nextString();
        String command = inputCommand.trim().toLowerCase();
        String info = commands.get(command);
        if (info == null)
            throw new SMABotUserException("Unknown command: " + inputCommand);
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Help (" + command + ")")
                        .setDescription(info)
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }
}