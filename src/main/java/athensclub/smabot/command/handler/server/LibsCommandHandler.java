package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.SMABotUtil;
import athensclub.smabot.command.*;
import athensclub.smabot.command.guard.Guard;
import athensclub.smabot.command.handler.CommandHandler;
import athensclub.smabot.player.Library;
import athensclub.smabot.player.SimpleSongInfo;
import athensclub.smabot.player.SongSearcher;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class LibsCommandHandler implements CommandHandler<ServerCommandData> {

    /**
     * Create a text displaying library list from the given list and display it as the libraries
     * of the user with the given mention tag.
     *
     * @param mentionTag  the mention tag of the user to display.
     * @param libraryList the list of libraries to display.
     * @param showPrivate whether the text will show the private library.
     * @return a text displaying the list of libraries.
     */
    private static String libraryList(String mentionTag, List<Library> libraryList, boolean showPrivate) {
        StringBuilder msg = new StringBuilder();
        msg.append("------------------------------\n")
                .append("**Libraries of ")
                .append(mentionTag)
                .append("**: ");
        if (!libraryList.isEmpty()) {
            msg.append('\n');
            int i = 1;
            for (Library lib : libraryList)
                writeLibrary(i++, msg, lib, showPrivate);
        } else {
            msg.append("None");
        }
        msg.append('\n')
                .append("If you are in server and want to view your hidden library, use the command in my DM.");
        return msg.toString();
    }

    private static void writeLibrary(int idx, StringBuilder msg, Library lib, boolean showPrivate) {
        msg.append("------------------------------\n")
                .append("**Library #")
                .append(idx)
                .append("**: ");
        if (!showPrivate && lib.isPrivate()) {
            msg.append("**(Private Library)**\n");
        } else if (lib.getSongs().isEmpty()) {
            msg.append("Empty\n");
        } else {
            msg.append("\n");
            int i = 1;
            for (SimpleSongInfo song : lib.getSongs()) {
                msg.append(i++)
                        .append(") **")
                        .append(song.getTitle())
                        .append("** (")
                        .append(song.getUri())
                        .append(")\n");
            }
        }
    }

    /**
     * Display the library list of the user of the command, if the command scanner does not has
     * any arguments to consume.
     *
     * @param scanner     the {@link CommandScanner} instance to parse command.
     * @param data        the {@link CommandData} instance.
     * @param showPrivate whether the display text will also display the private library.
     * @return {@code true} if the scanner does not have any arguments and this does display the
     * library list, otherwise {@code false}.
     */
    public static boolean displayUserLibraryListIfNoArg(CommandScanner scanner, CommandData data, boolean showPrivate) {
        if (!scanner.hasNext()) {
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Library")
                            .setDescription(libraryList(data.getUser().getNicknameMentionTag(),
                                    data.getUserProvider().libraryGetAll(data.getUser().getIdAsString()), showPrivate))
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return true;
        }
        return false;
    }

    private static void play(ServerCommandData data, CommandScanner scanner, SongSearcher songSearcher) {
        Guard.PERMISSION_GUARD.guardSpecific(Permission.PERMISSION_PLAY, data);
        Guard.VOICE_CHANNEL_ONLY_GUARD.guard(data);

        CommandArgument arg1 = scanner.next();
        Optional<User> target = arg1.asUserOptional();
        if(target.isPresent()){
            int index = getIndex(scanner.next(), data.getUserProvider().libraryCount(target.get().getIdAsString()));
            Library lib = data.getUserProvider().libraryGet(target.get().getIdAsString(), index);
            if(lib.isPrivate())
                throw new SMABotUserException("Library #" + index + " of " +
                        target.get().getNicknameMentionTag() + " is private!");
            addSongsToQueue(data, lib.getSongs(), songSearcher);
            return;
        }

        int index = getIndex(arg1, data.getUserProvider().libraryCount(data.getUser().getIdAsString()));
        Library lib = data.getUserProvider().libraryGet(data.getUser().getIdAsString(), index);
        addSongsToQueue(data, lib.getSongs(), songSearcher);
    }

    private static void addSongsToQueue(ServerCommandData data,List<SimpleSongInfo> songs, SongSearcher songSearcher){
        data.getSMABotServer().setTextChannel(data.getTextChannel());
        for (SimpleSongInfo song : songs) {
            try {
                songSearcher.load(song.getUri(), new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        SMABotUtil.runWithException(() -> { // run with exception because of banned songs will throw exception.
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
                                        .setColor(Color.RED))
                                .send(data.getTextChannel());
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        new MessageBuilder()
                                .setEmbed(new EmbedBuilder()
                                        .setTitle("Error")
                                        .setDescription(":x: " + data.getUser().getNicknameMentionTag() + ", " + exception.getMessage()))
                                .send(data.getTextChannel());
                    }
                }).get(); //get() so it goes in order
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Do the command {@code libs show <index>} with the given scanner and command data.
     *
     * @param data  the {@link CommandData} instance.
     * @param index the library index.
     */
    public static void hide(CommandData data, int index) {
        data.getUserProvider().librarySetVisibility(data.getUser().getIdAsString(), index, true);
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Library")
                        .setDescription("Successfully made library #" + index + " private." +
                                "Note that any info about this library before this command will still be displayed!")
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }

    /**
     * Do the command {@code libs hide <index>} with the given scanner and command data.
     *
     * @param data  the {@link CommandData} instance.
     * @param index the library index.
     */
    public static void show(CommandData data, int index) {
        data.getUserProvider().librarySetVisibility(data.getUser().getIdAsString(), index, false);
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Library")
                        .setDescription("Successfully made library #" + index + " public")
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }

    /**
     * Do the command {@code libs rr <index> ...} with the given scanner and command data.
     *
     * @param data    the {@link CommandData} instance.
     * @param scanner the {@link CommandScanner} instance that will consume the command arguments after the library index.
     * @param index   the library index.
     */
    public static void rr(CommandData data, CommandScanner scanner, int index) {
        int removed = data.getUserProvider()
                .libraryRemoveRange(data.getUser().getIdAsString(), index, scanner.next().asInt(), scanner.next().asInt())
                .size();
        new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("Library")
                        .setDescription("Successfully removed " + removed + " songs from library #" + index)
                        .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }

    /**
     * Do the command {@code libs add <index> ...} with the given arguments.
     *
     * @param data         the {@link CommandData} instance.
     * @param scanner      the {@link CommandScanner} instance that will consume the command arguments after the library index.
     * @param index        the library index.
     * @param songSearcher the {@link SongSearcher} instance responsible for searching songs from user query.
     */
    public static void add(CommandData data, CommandScanner scanner,
                           int index, SongSearcher songSearcher) {
        String query = scanner.remaining();
        songSearcher.load(query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                AudioTrackInfo info = track.getInfo();
                data.getUserProvider()
                        .libraryAddSong(data.getUser().getIdAsString(), index, new SimpleSongInfo(track));
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Library")
                                .setDescription("Successfully added **" + info.title + "** (" +
                                        info.uri + ") to library #" + index + "!")
                                .setColor(Color.GREEN))
                        .send(data.getTextChannel());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks())
                    data.getUserProvider()
                            .libraryAddSong(data.getUser().getIdAsString(), index, new SimpleSongInfo(track));
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setTitle("Library")
                                .setDescription("Successfully added " + playlist.getTracks().size() +
                                        " songs to library #" + index + "!")
                                .setColor(Color.GREEN))
                        .send(data.getTextChannel());
            }

            @Override
            public void noMatches() {
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder()
                                .setThumbnail("Song not found")
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
                                .setColor(Color.GREEN))
                        .send(data.getTextChannel());
            }
        });
    }

    /**
     * Parse the given {@link CommandArgument} as the library index and returning it. This will
     * throw {@link SMABotUserException} if the read index is invalid or is out of library count
     * range.
     * @param arg          the {@link CommandArgument} to parse.
     * @param libraryCount the number of libraries that index can be before causing
     *                     {@link SMABotUserException}
     * @return the parsed index.
     */
    public static int getIndex(CommandArgument arg, int libraryCount) {
        int index = arg.asInt();
        if (index - 1 < 0 || index - 1 >= libraryCount)
            throw new SMABotUserException("Library index out of range.");
        return index;
    }

    private final SongSearcher songSearcher;

    public LibsCommandHandler(SongSearcher songSearcher) {
        this.songSearcher = songSearcher;
    }

    @Override
    public void handle(ServerCommandData data) {
        CommandScanner scanner = data.scanner();
        if (displayUserLibraryListIfNoArg(scanner, data, false))
            return;

        CommandArgument arg1 = scanner.next();
        Optional<User> target = arg1.asUserOptional();
        if (target.isPresent()) {
            User targetUser = target.get();
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Library")
                            .setDescription(libraryList(targetUser.getNicknameMentionTag(),
                                    data.getUserProvider().libraryGetAll(targetUser.getIdAsString()), false))
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }

        String action = arg1.getValue().toLowerCase();
        if (action.equals("new")) {
            data.getUserProvider().libraryNew(data.getUser().getIdAsString());
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Library")
                            .setDescription("Successfully created new library!")
                            .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }

        if(action.equals("play")){
            play(data, scanner, songSearcher);
            return;
        }

        //assume the command will require a library index.
        int index = getIndex(scanner.next(), data.getUserProvider().libraryCount(data.getUser().getIdAsString()));
        switch (action.toLowerCase()) {
            case "add" -> add(data, scanner, index, songSearcher);
            case "rr" -> rr(data, scanner, index);
            case "hide" -> hide(data, index);
            case "show" -> show(data, index);
            default -> throw new SMABotUserException("Unknown libs command: " + action);
        }

    }

}
