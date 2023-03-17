package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.manager.VoteManager;
import athensclub.smabot.command.Permission;

public class LeaveCommandHandler extends VoteBasedCommandHandler {

    @Override
    protected void checkCanVote(ServerCommandData data) {
        if(data.getSMABotServer().getVoiceChannel() == null)
            throw new SMABotUserException("I'm not in any voice channel. I can't leave voice channel.");
    }

    @Override
    protected VoteManager getVoteManager(CommandData data) {
        return data.getSMABotServer().getLeaveManager();
    }

    @Override
    protected Permission forcePermission() {
        return Permission.PERMISSION_FORCE_LEAVE;
    }

    @Override
    protected void onAction(CommandData data) {
        data.getSMABotServer().leaveAndDisplayMessage();
    }

    @Override
    protected String getVoteAction() {
        return "make this bot leave voice channel";
    }

}
