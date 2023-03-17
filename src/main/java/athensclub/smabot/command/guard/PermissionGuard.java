package athensclub.smabot.command.guard;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.Permission;
import athensclub.smabot.command.ServerCommandData;

/**
 * Guard the user to have specific permission (in case of normal commands, the user must have
 * the permission with the same name as the command name to use the command).
 */
public class PermissionGuard implements Guard<ServerCommandData> {

    /**
     * Guard the given user to have the given permission or else {@link athensclub.smabot.SMABotUserException}
     * will be thrown
     *
     * @param permission the permission to guard the user.
     * @param data       the command data instance.
     */
    public void guardSpecific(Permission permission, ServerCommandData data) {
        if (!data.getServerProvider().hasPermission(data.getServer(), data.getUser(), permission))
            throw new SMABotUserException("You must have permission " + permission.getName() + " to use this command!");
    }

    @Override
    public void guard(ServerCommandData data) {
        guardSpecific(data.getSMABotServer().getPermissionManager().getPermission(data.getCommand().getName()), data);
    }
}
