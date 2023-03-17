package athensclub.smabot.command.handler.server;

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

public class UnbanCommandHandler implements CommandHandler<ServerCommandData> {

    private final SongSearcher searcher;

    public UnbanCommandHandler(SongSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public void handle(ServerCommandData data) {
        searcher.load(data.getCommandBody(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                AudioTrackInfo info = track.getInfo();
                data.getSMABotServer().unban(track);
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Unban")
                                .setDescription("Successfully unbanned **" + info.title + "** (" + info.uri +
                                        ") from this server!")
                                .setColor(Color.GREEN));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks())
                    data.getSMABotServer().unban(track);
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Unban")
                                .setDescription("Successfully unbanned " +
                                        playlist.getTracks().size() + " songs from this server!")
                                .setColor(Color.GREEN))
                        .send(data.getTextChannel());
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
            }
        });
    }

}
