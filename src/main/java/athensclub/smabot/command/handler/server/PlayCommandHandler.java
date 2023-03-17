package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUtil;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import athensclub.smabot.player.SongSearcher;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

public class PlayCommandHandler implements CommandHandler<ServerCommandData> {

    private final SongSearcher searcher;

    public PlayCommandHandler(SongSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public void handle(ServerCommandData data) {
        data.getSMABotServer().setTextChannel(data.getTextChannel());
        searcher.load(data.getCommandBody(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                SMABotUtil.runWithException(() -> { // run with exception because of banned songs.
                    AudioTrackInfo info = track.getInfo();
                    data.getSMABotServer().getQueue().addSong(track);
                    new MessageBuilder()
                            .setEmbed(new EmbedBuilder()
                                    .setTitle("Queued")
                                    .setDescription("Added " + info.title + " (" + info.uri + ") to the queue!")
                                    .setColor(Color.GREEN))
                            .send(data.getTextChannel());
                    data.getSMABotServer().joinIfFree(data.getVoiceChannel());
                }, data.getTextChannel());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                SMABotUtil.runWithException(() -> { // run with exception because of banned songs.
                    for (AudioTrack song : playlist.getTracks())
                        data.getSMABotServer().getQueue().addSong(song);
                    new MessageBuilder()
                            .setEmbed(new EmbedBuilder()
                                    .setTitle("Queued")
                                    .setDescription("Added " + playlist.getTracks().size() + " songs to the queue!")
                                    .setColor(Color.GREEN))
                            .send(data.getTextChannel());
                    data.getSMABotServer().joinIfFree(data.getVoiceChannel());
                }, data.getTextChannel());
            }

            @Override
            public void noMatches() {
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Song not found")
                                .setDescription(data.getUser().getNicknameMentionTag() + ", Song not found!")
                                .setColor(Color.GREEN))
                        .send(data.getTextChannel());
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Error")
                                .setDescription(":x: " + data.getUser().getNicknameMentionTag() + ", " + exception.getMessage())
                                .setColor(Color.RED))
                        .send(data.getTextChannel());
                exception.printStackTrace();
            }
        });
    }

}
