package athensclub.smabot.command.guard;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.ServerCommandData;
import org.javacord.api.entity.channel.ServerVoiceChannel;

/**
 * A guard that require the user to be in voice channel and the same voice channel as this bot
 * before using the command.
 */
public class VoiceChannelOnlyGuard implements Guard<ServerCommandData> {

    @Override
    public void guard(ServerCommandData data) {
        if (data.getVoiceChannel() == null)
            throw new SMABotUserException(data.getUser().getNicknameMentionTag() + ", You must be in a voice channel to use this command!");

        ServerVoiceChannel serverChannel = data.getSMABotServer().getVoiceChannel();
        if (serverChannel != null && data.getVoiceChannel().getId() != serverChannel.getId())
            throw new SMABotUserException(data.getUser().getNicknameMentionTag() + ", You must be in a the same voice channel as me to use this command!");
    }

}
