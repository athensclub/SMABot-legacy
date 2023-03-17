package athensclub.smabot.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.data.provider.ServerProvider;
import athensclub.smabot.manager.AutoLeaveManager;
import athensclub.smabot.manager.PermissionManager;
import athensclub.smabot.manager.VoteManager;
import athensclub.smabot.player.LavaPlayerAudioSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.Map.Entry;

/**
 * A class representing a server. This will manage the bot activities in each server.
 */
public class SMABotServer {

    private final LavaPlayerAudioSource audioSource;

    private final AudioPlayer audioPlayer;

    private ServerVoiceChannel voiceChannel;

    private TextChannel textChannel;

    private Message currentlyPlayingMessage;

    private AudioConnection connection;

    private final ServerQueue queue;

    private final PermissionManager permissionManager;

    private final AutoLeaveManager autoLeaveManager;

    private final VoteManager leaveManager;

    private final VoteManager skipManager;

    private final AtomicBoolean playing;

    private final Object connectionLock;

    private final String id;

    private final ServerProvider provider;

    public SMABotServer(DiscordApi api, AudioPlayerManager audioPlayerManager, PermissionManager permissionManager,
                        String id, ServerProvider provider) {
        this.id = id;
        this.provider = provider;
        connectionLock = new Object();

        autoLeaveManager = new AutoLeaveManager(this);
        leaveManager = new VoteManager(this, this::leaveAndDisplayMessage);
        skipManager = new VoteManager(this, this::skip);

        queue = new ServerQueue(this);

        playing = new AtomicBoolean(false);
        this.permissionManager = permissionManager;

        audioPlayer = audioPlayerManager.createPlayer();
        audioSource = new LavaPlayerAudioSource(api, audioPlayer);
        audioPlayer.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                if (endReason.mayStartNext) {
                    queue.nextIndex();
                    play();
                } else
                    playing.set(false);
            }

            @Override
            public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
                super.onTrackException(player, track, exception);
                exception.printStackTrace();
                playing.set(false);
            }

            @Override
            public void onTrackStart(AudioPlayer player, AudioTrack track) {
                super.onTrackStart(player, track);
                skipManager.reset();
                AudioTrackInfo info = track.getInfo();
                if (provider.isAnnounce(id)) {
                    new MessageBuilder()
                            .setEmbed(new EmbedBuilder()
                                    .setTitle("Currently playing")
                                    .setDescription(":notes: Currently playing **" + info.title + "** (" + info.uri + ")")
                                    .setColor(Color.GREEN))
                            .send(textChannel)
                            .thenAccept(m -> currentlyPlayingMessage = m);
                }
            }
        });
    }

    /**
     * Play the song in the current queue
     */
    private void play() {
        if (currentlyPlayingMessage != null) {
            currentlyPlayingMessage.delete();
            currentlyPlayingMessage = null;
        }

        if (queue.currentSong() == null) {
            disconnect();
            return;
        }
        audioPlayer.playTrack(queue.currentSong().makeClone());
    }

    /**
     * Play the queue if not already playing.
     */
    protected void playIfNotPlaying() {
        if (!playing.get()) {
            playing.set(true);
            play();
        }
    }

    /**
     * Skip the currently playing song.
     */
    public void skip() {
        queue.nextIndex();
        play();
    }

    /**
     * Make this bot join the given channel, if this bot is not in any channel of this server.
     *
     * @param channel the channel to join to.
     */
    public void joinIfFree(ServerVoiceChannel channel) {
        synchronized (connectionLock) {
            if (connection == null) {
                voiceChannel = channel;
                channel.connect().thenAccept(c -> {
                    connection = c;
                    connection.setAudioSource(audioSource);
                });
            }
        }
    }

    /**
     * @return The count of non-bot user in the voice channel that this bot is in.
     */
    public int nonBotVoiceChannelMembers() {
        return voiceChannel == null ? 0 :
                (int) voiceChannel.getConnectedUsers()
                        .stream()
                        .filter(u -> !u.isBot())
                        .count();
    }

    /**
     * Reset the following:
     * <ul>
     *     <li>Reset the queue state using {@link ServerQueue#reset()}</li>
     *     <li>Set the playing property to false</li>
     *     <li>Delete currently playing message</li>
     *     <li>Set the message channel of this server to null</li>
     * </ul>
     */
    private void reset() {
        playing.set(false);
        queue.reset();
        if (currentlyPlayingMessage != null) {
            currentlyPlayingMessage.delete();
            currentlyPlayingMessage = null;
        }
    }

    /**
     * Display the message to discord indicating that this bot is leaving voice channel
     * and disconnecting from the voice channel.
     */
    public void leaveAndDisplayMessage() {
        if (voiceChannel == null)
            throw new SMABotUserException("I'm not in a voice channel.");
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Disconnect")
                        .setDescription("I am leaving the voice channel!")
                        .setColor(Color.GREEN))
                .send(textChannel);
        disconnect();
    }

    /**
     * Disconnect the bot from this server's voice channel and reset the server's state using
     * {@link SMABotServer}'s internal {@link SMABotServer#reset()}.
     */
    public void disconnect() {
        synchronized (connectionLock) {
            if (connection != null)
                connection.close();
            voiceChannel = null;
            connection = null;
            reset();
        }
    }

    /**
     * Called to make the bot update its data to be up to date with this server's voice channel
     * that this bot is in.
     */
    public void updateVoiceState() {
        autoLeaveManager.updateVoiceState();
    }

    /**
     * @return All the banned songs in this server, as a {@link Stream} of {@link Entry},
     * where the key is the uri of the song, and the value is the title of the song.
     */
    public Stream<Entry<String, String>> getAllBannedSongs() {
        return provider.getAllBannedSongs(id).entrySet().stream();
    }

    /**
     * Ban the given song from this server.
     *
     * @param track the track to be banned.
     */
    public void ban(AudioTrack track) {
        AudioTrackInfo info = track.getInfo();
        provider.ban(id, info.uri, info.title);
    }

    /**
     * Unban the given song from this server, if it is banned from this server.
     *
     * @param track the track to be unbanned.
     */
    public void unban(AudioTrack track) {
        provider.unban(id, track.getInfo().uri);
    }

    /**
     * Check whether the given track is banned from this server.
     *
     * @param track the track to check.
     * @return whether the given track is banned from this server.
     */
    public boolean isBanned(AudioTrack track) {
        return provider.isBanned(id, track.getInfo().uri);
    }

    /**
     * Toggle whether this bot will announce the currently playing song.
     */
    public void toggleAnnounce() {
        provider.toggleAnnounce(id);
    }

    /**
     * @return whether this bot will announce the currently playing song.
     */
    public boolean isAnnounce() {
        return provider.isAnnounce(id);
    }

    /**
     * @return The voice channel that this bot is currently in.
     */
    public ServerVoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public ServerQueue getQueue() {
        return queue;
    }

    public void setTextChannel(TextChannel textChannel) {
        this.textChannel = textChannel;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public VoteManager getLeaveManager() {
        return leaveManager;
    }

    public VoteManager getSkipManager() {
        return skipManager;
    }

}
