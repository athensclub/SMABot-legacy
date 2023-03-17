package athensclub.smabot.handler;

import athensclub.smabot.Main;
import athensclub.smabot.SMABotUtil;
import athensclub.smabot.command.Command;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.guard.Guard;
import athensclub.smabot.command.handler.CommandHandler;
import athensclub.smabot.data.provider.DataProvider;
import athensclub.smabot.manager.CommandManager;
import athensclub.smabot.manager.ServerManager;
import athensclub.smabot.server.SMABotServer;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MessageHandler implements MessageCreateListener {

    private final CommandManager<ServerCommandData> serverCommandManager;

    private final CommandManager<CommandData> dmCommandManager;

    private final ServerManager serverManager;

    private final DataProvider dataProvider;

    private final DiscordApi discordApi;

    public MessageHandler(CommandManager<ServerCommandData> serverCmd, CommandManager<CommandData> dmCmd,
                          ServerManager server, DataProvider provider, DiscordApi api) {
        serverCommandManager = serverCmd;
        dmCommandManager = dmCmd;
        serverManager = server;
        dataProvider = provider;
        discordApi = api;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessage().getAuthor().isBotUser())
            return;
        if (event.isPrivateMessage()) {
            handleDM(event);
            return;
        }
        //event.getMessageAuthor().asUser().get()
        String msg = guardPrefix(event);
        if (msg == null)
            return;
        Entry<String, String> nameAndBody = parseCommand(msg);
        String name = nameAndBody.getKey();
        String body = nameAndBody.getValue();
        Entry<ServerCommandData, CommandHandler<ServerCommandData>> cmd = parseCommand(name, body, event);
        if (cmd == null || !guardBasic(cmd.getKey()))
            return;
        SMABotUtil.runWithException(() -> cmd.getValue().handle(cmd.getKey()),
                cmd.getKey().getTextChannel());
    }

    private void handleDM(MessageCreateEvent event) {
        Entry<String, String> nameAndBody = parseCommand(event.getMessageContent());
        String name = nameAndBody.getKey();
        String body = nameAndBody.getValue();
        Entry<Command, CommandHandler<CommandData>> command = dmCommandManager.getCommand(name);
        if (command == null) {
            event.getChannel().sendMessage("Unknown command!");
            return;
        }
        SMABotUtil.runWithException(() -> command.getValue()
                        .handle(new CommandData(command.getKey(), body, event, null, dataProvider, discordApi)),
                event.getChannel());
    }

    /**
     * Guard the command for the basic requirements, which is the following
     * <ul>
     *     <li>User must have the permission to use the given command</li>
     *     <li>User must be in Voice Channel, if the command is voice channel only.</li>
     * </ul>
     *
     * @param data the {@link ServerCommandData} instance.
     * @return {@code true} if the command passes the guard, otherwise {@code false}.
     */
    private boolean guardBasic(ServerCommandData data) {
        if(!dataProvider.getServerProvider().isListeningTo(data.getServer().getIdAsString(), data.getTextChannel().getIdAsString()))
            return false; // guard is listening to the server
        boolean[] passRef = new boolean[1];
        SMABotUtil.runWithException(() -> {
            Guard.PERMISSION_GUARD.guard(data);
            if (data.getCommand().isVoiceChannelOnly())
                Guard.VOICE_CHANNEL_ONLY_GUARD.guard(data);
            passRef[0] = true;
        }, data.getTextChannel());
        return passRef[0];
    }

    /**
     * Guard the prefix and remove the prefix from the message.
     *
     * @param event the {@link MessageCreateEvent} instance.
     * @return a command message without the prefix if the command has the prefix, otherwise {@code null}.
     */
    private String guardPrefix(MessageCreateEvent event) {
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        String prefix = dataProvider.getServerProvider().getPrefix(event.getServer().get().getIdAsString()).orElse(Main.BUILD_MODE.prefix);
        if (event.getMessageContent().startsWith(prefix))
            return event.getMessageContent().substring(prefix.length());
        return null;
    }

    /**
     * Parse the given command by splitting it into the command name and command body.
     *
     * @param message the command message to parse.
     * @return an {@link Entry} instance where the key is the name of the command, and the value
     * is the command body.
     */
    private Entry<String, String> parseCommand(String message) {
        String name = message;
        String body = "";
        int idx = message.indexOf(" ");
        if (idx >= 0) {
            name = message.substring(0, idx);
            body = trimFront(message.substring(idx + 1));
        }
        return Map.entry(name, body);
    }

    private String trimFront(String str) {
        return str.codePoints()
                .dropWhile(Character::isWhitespace)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Parse the command and see if its known command or not.
     *
     * @param name  the name of the command.
     * @param body  the command body.
     * @param event the {@link MessageCreateEvent} instance.
     * @return a pair of command data and its handler.
     */
    private Entry<ServerCommandData, CommandHandler<ServerCommandData>> parseCommand(String name, String body, MessageCreateEvent event) {
        Entry<Command, CommandHandler<ServerCommandData>> command = serverCommandManager.getCommand(name);
        if (command == null) {
            event.getMessage()
                    .getChannel()
                    .sendMessage(event.getMessage()
                            .getAuthor()
                            .asUser()
                            .map(u -> u.getNicknameMentionTag() + ", ")
                            .orElse("") + "Command not found!");
            return null;
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        SMABotServer server = serverManager.getServer(event.getServer().get().getIdAsString());

        return Map.entry(new ServerCommandData(command.getKey(), body, event, server, dataProvider, discordApi), command.getValue());
    }


}
