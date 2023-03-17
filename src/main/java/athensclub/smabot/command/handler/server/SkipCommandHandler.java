package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.Permission;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.manager.VoteManager;

public class SkipCommandHandler extends VoteBasedCommandHandler {
    @Override
    protected void checkCanVote(ServerCommandData data) {
        if (data.getSMABotServer().getQueue().currentSong() == null)
            throw new SMABotUserException("There are currently no songs playing. You can't skip the queue.");
    }

    @Override
    protected VoteManager getVoteManager(CommandData data) {
        return data.getSMABotServer().getSkipManager();
    }

    @Override
    protected Permission forcePermission() {
        return Permission.PERMISSION_FORCE_SKIP;
    }

    @Override
    protected void onAction(CommandData data) {
        data.getSMABotServer().skip();
    }

    @Override
    protected String getVoteAction() {
        return "skip the current track";
    }
}
