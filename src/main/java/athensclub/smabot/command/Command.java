package athensclub.smabot.command;

import athensclub.smabot.command.handler.CommandHandler;
import athensclub.smabot.command.handler.dm.DMLibsCommandHandler;
import athensclub.smabot.command.handler.server.*;
import athensclub.smabot.player.SongSearcher;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a single command.
 */
public class Command {

    /**
     * Create a mapping from alias to server command of all aliases available by default for SMABot.
     *
     * @return A mapping from alias to server command of all aliases available by default for SMABot.
     */
    public static Map<String, String> createServerAliases() {
        return Map.ofEntries(Map.entry("p", "play"));
    }

    /**
     * Create a map of all server commands from the given arguments.
     *
     * @param searcher the {@link SongSearcher} instance responsible for searching songs from user query.
     * @return list of all server commands.
     */
    public static Map<Command, CommandHandler<ServerCommandData>> createServerCommands(SongSearcher searcher) {
        return Map.ofEntries(Map.entry(new Command("play", true), new PlayCommandHandler(searcher)),
                Map.entry(new Command("leave", true), new LeaveCommandHandler()),
                Map.entry(new Command("skip", true), new SkipCommandHandler()),
                Map.entry(new Command("queue", false), new QueueCommandHandler()),
                Map.entry(new Command("loop", true), new LoopCommandHandler()),
                Map.entry(new Command("help", false), new HelpCommandHandler()),
                Map.entry(new Command("perms", false), new PermsCommandHandler()),
                Map.entry(new Command("rr", true), new RRCommandHandler()),
                Map.entry(new Command("ban", false), new BanCommandHandler(searcher)),
                Map.entry(new Command("unban", false), new UnbanCommandHandler(searcher)),
                Map.entry(new Command("shuffle", true), new ShuffleCommandHandler()),
                Map.entry(new Command("prefix", false), new PrefixCommandHandler()),
                Map.entry(new Command("announce", false), new AnnounceCommandHandler()),
                Map.entry(new Command("libs", false), new LibsCommandHandler(searcher)),
                Map.entry(new Command("listen", false), new ListenCommandHandler()),
                Map.entry(new Command("nolisten", false), new NoListenCommandHandler()),
                Map.entry(new Command("jail",false), new JailCommandHandler()),
                Map.entry(new Command("unjail",false), new UnjailCommandHandler()));
    }

    /**
     * Create a list of DM commands from the given arguments.
     *
     * @param songSearcher the {@link SongSearcher} instance responsible for searching songs from user query.
     * @return list of all DM commands.
     */
    public static Map<Command, CommandHandler<CommandData>> createDMCommands(SongSearcher songSearcher) {
        return Map.ofEntries(Map.entry(new Command("libs", false), new DMLibsCommandHandler(songSearcher)));
    }

    private final String name;

    private final boolean voiceChannelOnly;

    public Command(String name, boolean voiceChannelOnly) {
        this.name = name;
        this.voiceChannelOnly = voiceChannelOnly;
    }

    public boolean isVoiceChannelOnly() {
        return voiceChannelOnly;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Command)) return false;
        Command command = (Command) o;
        return name.equals(command.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
