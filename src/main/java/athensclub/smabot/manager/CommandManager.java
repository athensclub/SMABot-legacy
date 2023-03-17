package athensclub.smabot.manager;

import athensclub.smabot.command.Command;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.handler.CommandHandler;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A container for all the commands.
 */
public class CommandManager<T extends CommandData> {

    private final HashMap<String, Entry<Command, CommandHandler<T>>> commands;

    private final HashMap<String, String> aliases;

    public CommandManager() {
        commands = new HashMap<>();
        aliases = new HashMap<>();
    }

    /**
     * Add the alias for the given command to this manager.
     *
     * @param alias   the alias for the command that the user can use interchangably with the command.
     * @param command the command to add the alias for.
     */
    public void setAlias(String alias, String command) {
        if(!commands.containsKey(command))
            throw new IllegalArgumentException("Add alias for unknown command: " + command + ", alias=" + alias);
        aliases.put(alias, command);
    }

    /**
     * Register the command with handler to this manager.
     *
     * @param cmd     the command to add.
     * @param handler the handler for the command.
     */
    public void registerCommand(Command cmd, CommandHandler<T> handler) {
        commands.put(cmd.getName().toLowerCase(), new SimpleEntry<>(cmd, handler));
    }

    /**
     * Get the command by name.
     *
     * @param name the name of the command (case insensitive).
     * @return the command with the given name.
     */
    public Entry<Command, CommandHandler<T>> getCommand(String name) {
        Entry<Command, CommandHandler<T>> temp = commands.get(name.toLowerCase());
        if(temp != null)
            return temp;
        return commands.get(aliases.get(name.toLowerCase()));
    }

}
