package athensclub.smabot.command.guard;

import athensclub.smabot.command.CommandData;

/**
 * A command-level guarding.
 */
public interface Guard<T extends CommandData> {

    /**
     * Guard the user so that the user has to be in a voice channel and the same voice channel is this
     * bot to use the command.
     */
    VoiceChannelOnlyGuard VOICE_CHANNEL_ONLY_GUARD = new VoiceChannelOnlyGuard();

    /**
     * Guard the user to have specific permission (in case of normal commands, the user must have
     * the permission with the same name as the command name to use the command).
     */
    PermissionGuard PERMISSION_GUARD = new PermissionGuard();

    /**
     * Guard the given command, throwing {@link athensclub.smabot.SMABotUserException} when
     * the data does not pass the guard.
     *
     * @param data the command to guard.
     */
    void guard(T data);

}
