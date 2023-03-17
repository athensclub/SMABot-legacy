package athensclub.smabot.command;

import athensclub.smabot.data.provider.DataProvider;
import athensclub.smabot.data.provider.ServerProvider;
import athensclub.smabot.data.provider.UserProvider;
import athensclub.smabot.server.SMABotServer;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

public class CommandData {

    private final Command command;

    private final String commandBody;

    private final MessageCreateEvent event;

    private final SMABotServer smaBotServer;

    private final ServerProvider serverProvider;

    private final UserProvider userProvider;

    private final TextChannel textChannel;

    private final User user;

    private final DiscordApi api;

    public CommandData(Command cmd, String body, MessageCreateEvent event, SMABotServer smaBotServer,
                       DataProvider provider, DiscordApi api) {
        commandBody = body;
        command = cmd;
        serverProvider = provider.getServerProvider();
        userProvider = provider.getUserProvider();
        this.event = event;
        this.smaBotServer = smaBotServer;
        this.api = api;
        //noinspection OptionalGetWithoutIsPresent
        user = event.getMessageAuthor().asUser().get();
        textChannel = event.getChannel();
    }

    /**
     * Create a new command scanner from this command's body.
     *
     * @return a new command scanner that starts scanning from command body.
     */
    public CommandScanner scanner() {
        return new CommandScanner(commandBody, api);
    }

    public DiscordApi getApi() {
        return api;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public User getUser() {
        return user;
    }

    public ServerProvider getServerProvider() {
        return serverProvider;
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }

    public MessageCreateEvent getEvent() {
        return event;
    }

    public SMABotServer getSMABotServer() {
        return smaBotServer;
    }

    public Command getCommand() {
        return command;
    }

    public String getCommandBody() {
        return commandBody;
    }

}
