package athensclub.smabot.manager;

import athensclub.smabot.server.SMABotServer;

import java.util.HashSet;
import java.util.Set;

public class VoteManager {

    private final SMABotServer server;

    private final Set<String> voted;

    private final Runnable onVotePass;

    /**
     * Represent a result after a vote has been made.
     */
    public static class VoteResult {

        /**
         * Whether the user has already voted, and this vote has no effect.
         */
        public final boolean alreadyVoted;

        /**
         * Whether this vote makes the total vote surpass half the people, triggering
         * vote passed event.
         */
        public final boolean votePassed;

        public VoteResult(boolean voted, boolean passed) {
            alreadyVoted = voted;
            votePassed = passed;
        }

    }

    public VoteManager(SMABotServer server, Runnable onPass) {
        this.server = server;
        onVotePass = onPass;
        voted = new HashSet<>();
    }

    /**
     * Register the given user to vote in this vote manager.
     *
     * @param user the snowflake id of the user to vote.
     * @return the result of the vote as a {@link VoteResult} instance.
     */
    public VoteResult vote(String user) {
        if (voted.contains(user))
            return new VoteResult(true, false);
        voted.add(user);
        if (checkPass())
            return new VoteResult(false, true);
        return new VoteResult(false, false);
    }

    /**
     * Reset the state of this vote manager including:
     * <ul>
     *     <li>The people voted</li>
     * </ul>
     */
    public void reset() {
        voted.clear();
    }

    /**
     * Perform a calculations to update the state of this manager to match with the update of
     * the voice channel. Called when voice channel updates.
     */
    public void updateVoiceChannel() {
        checkPass();
    }

    /**
     * Check if the vote has passed yet, and do the action on vote pass if already passed and
     * reset the state.
     *
     * @return Whether the vote has passed after this check.
     */
    private boolean checkPass() {
        if (voted.size() >= server.nonBotVoiceChannelMembers() / 2) {
            onVotePass.run();
            reset();
            return true;
        }
        return false;
    }

    /**
     * Get the ratio between the voted users, and the amount of people required to make
     * the voted pass in form of string (in format "(%d/%d)").
     *
     * @return the ratio string.
     */
    public String getRatioString() {
        return "(" + voted.size() + "/" + server.nonBotVoiceChannelMembers() / 2 + ")";
    }

}
