package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.Permission;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import athensclub.smabot.manager.VoteManager;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

/**
 * A command that deals with {@link VoteManager} instance.
 */
public abstract class VoteBasedCommandHandler implements CommandHandler<ServerCommandData> {

    /**
     * Check whether the user can vote do this action at the current moment. Also check
     * whether the user can force the action. For example, users will not be able to vote for this
     * bot to leave the voice channel if the bot isn't in any of the voice channel. This should
     * throw {@link SMABotUserException} when the vote action isn't allowed.
     *
     * @param data the {@link CommandData} instance.
     * @throws SMABotUserException when the action isn't allowed.
     */
    protected abstract void checkCanVote(ServerCommandData data);

    /**
     * Get the {@link VoteManager} instance that will be used by this handler.
     *
     * @param data the {@link CommandData} instance.
     * @return the {@link VoteManager} instance that will be used by this handler.
     */
    protected abstract VoteManager getVoteManager(CommandData data);

    /**
     * @return The permission on which if the user has it, will make the user bypass the
     * voting system and force the vote to pass.
     */
    protected abstract Permission forcePermission();

    /**
     * This method will get called when user with force permission do the action.
     *
     * @param data the {@link CommandData} instance.
     */
    protected abstract void onAction(CommandData data);

    /**
     * @return the purpose of the vote, to display in {@code '${user_mention} has voted to ${getVoteAction()}'}
     */
    protected abstract String getVoteAction();

    @Override
    public void handle(ServerCommandData data) {
        checkCanVote(data);
        VoteManager manager = getVoteManager(data);

        if (data.getServerProvider().hasPermission(data.getServer(), data.getUser(), forcePermission())) {
            onAction(data);
            manager.reset();
            return;
        }

        VoteManager.VoteResult voteResult = manager.vote(data.getUser().getIdAsString());
        if (!voteResult.votePassed) {
            if (voteResult.alreadyVoted)
                throw new SMABotUserException("You already voted! You can not vote to " + getVoteAction() + " again.");
            else
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Vote")
                                .setDescription(data.getUser().getNicknameMentionTag()
                                        + " has voted to " + getVoteAction() + " "
                                        + manager.getRatioString() + "!")
                                .setColor(Color.GREEN))
                        .send(data.getTextChannel());
        }
    }
}


