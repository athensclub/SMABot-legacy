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
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

public class BanCommandHandler implements CommandHandler<ServerCommandData> {

    private final SongSearcher searcher;

    public BanCommandHandler(SongSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public void handle(ServerCommandData data) {
        if (data.getCommandBody().isBlank()) {
            List<Entry<String, String>> banned = data.getSMABotServer().getAllBannedSongs().collect(Collectors.toList());
            StringBuilder b = new StringBuilder();
            b.append("The banned songs in this server: ");
            if (!banned.isEmpty()) {
                b.append('\n');
                int i = 1;
                for (Entry<String, String> song : banned) {
                    b.append(i)
                            .append(". **")
                            .append(song.getValue())
                            .append("** (")
                            .append(song.getKey())
                            .append(")\n");
                }
            } else {
                b.append("None");
            }
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Ban")
                            .setDescription(b.toString())
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }

        searcher.load(data.getCommandBody(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                AudioTrackInfo info = track.getInfo();
                data.getSMABotServer().ban(track);
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Ban")
                                .setDescription("Successfully banned **" + info.title + "** (" + info.uri +
                                        ") from this server!")
                                .setColor(Color.GREEN))
                        .send(data.getTextChannel());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks())
                    data.getSMABotServer().ban(track);
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Ban")
                                .setDescription("Successfully banned " +
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
                                .setColor(Color.RED))
                        .send(data.getTextChannel());
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Error")
                                .setDescription(":x: " + data.getUser().getNicknameMentionTag() + exception.getMessage())
                                .setColor(Color.RED))
                        .send(data.getTextChannel());
            }
        });
    }
}
