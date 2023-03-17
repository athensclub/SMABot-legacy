package athensclub.smabot.command.handler;

import athensclub.smabot.command.CommandData;

/**
 * Generic command handler.
 */
public interface CommandHandler<T extends CommandData> {

    /**
     * Handle the command with the given data.
     *
     * @param data the command data.
     */
    void handle(T data);

}
