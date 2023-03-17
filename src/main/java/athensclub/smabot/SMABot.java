package athensclub.smabot;

import athensclub.smabot.api.google.YoutubeManager;
import athensclub.smabot.command.Command;
import athensclub.smabot.command.CommandData;
import athensclub.smabot.command.Permission;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.data.provider.DataProvider;
import athensclub.smabot.data.provider.impl.mongo.MongoDataProvider;
import athensclub.smabot.handler.MessageHandler;
import athensclub.smabot.handler.VoiceStateHandler;
import athensclub.smabot.manager.CommandManager;
import athensclub.smabot.manager.PermissionManager;
import athensclub.smabot.manager.ServerManager;
import athensclub.smabot.player.SongSearcher;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.*;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.user.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SMABot {

    public static final Logger LOG = Logger.getLogger(SMABot.class.getName());

    private DiscordApi api;

    private MessageHandler messageHandler;

    private VoiceStateHandler voiceStateHandler;

    private AudioPlayerManager audioManager;

    private CommandManager<ServerCommandData> serverCommandManager;

    private CommandManager<CommandData> dmCommandManager;

    private ServerManager serverManager;

    private PermissionManager permissionManager;

    private YoutubeManager youtubeManager;

    private SongSearcher songSearcher;

    private DataProvider dataProvider;

    /**
     * Initialize the bot and Login the bot using the given token and start the bot.
     *
     * @param token the token to use for login.
     */
    public void start(String token) throws ExecutionException, InterruptedException {
        api = new DiscordApiBuilder().setToken(token)
                .setAllIntentsExcept(Intent.GUILD_PRESENCES)
                .login().join();
        api.setMessageCacheSize(0, 0);

        dataProvider = new MongoDataProvider();

        audioManager = new DefaultAudioPlayerManager();

        audioManager.registerSourceManager(new YoutubeAudioSourceManager());
        SoundCloudDataReader soundCloudDataReader = new DefaultSoundCloudDataReader();
        SoundCloudHtmlDataLoader soundCloudHtmlDataLoader = new DefaultSoundCloudHtmlDataLoader();
        SoundCloudFormatHandler soundCloudFormatHandler = new DefaultSoundCloudFormatHandler();
        audioManager.registerSourceManager(new SoundCloudAudioSourceManager(false, soundCloudDataReader, soundCloudHtmlDataLoader, soundCloudFormatHandler, new DefaultSoundCloudPlaylistLoader(soundCloudHtmlDataLoader, soundCloudDataReader, soundCloudFormatHandler)));

        audioManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(audioManager);

        youtubeManager = new YoutubeManager();
        songSearcher = new SongSearcher(youtubeManager, audioManager);

        permissionManager = new PermissionManager();
        Permission.ALL_PERMISSIONS.forEach(permissionManager::add);

        serverManager = new ServerManager(audioManager, permissionManager, dataProvider.getServerProvider(), api);

        serverCommandManager = new CommandManager<>();
        dmCommandManager = new CommandManager<>();
        Command.createServerCommands(songSearcher)
                .forEach(serverCommandManager::registerCommand);
        Command.createServerAliases().forEach(serverCommandManager::setAlias);
        Command.createDMCommands(songSearcher)
                .forEach(dmCommandManager::registerCommand);

        messageHandler = new MessageHandler(serverCommandManager, dmCommandManager, serverManager, dataProvider, api);
        voiceStateHandler = new VoiceStateHandler(serverManager, dataProvider, api);

        api.addMessageCreateListener(messageHandler);
        api.addServerVoiceChannelMemberJoinListener(voiceStateHandler);
        api.addServerVoiceChannelMemberLeaveListener(voiceStateHandler);

        pickupJailEndtime();

        LOG.info("Finished Initialization");


    }

    private void pickupJailEndtime() {
        LOG.info("Picking up jail end time");
        dataProvider.getServerProvider().getAllJailed()
                .forEach((user, jailed) -> jailed.forEach((server, info) -> {
                    Instant endInstant = Instant.parse(info.getValue());
                    if (Instant.now().isBefore(endInstant)) {
                        try {
                            User target = api.getUserById(user).get();
                            if(target.getConnectedVoiceChannel(api.getServerById(server).get()).isPresent())
                                target.move(api.getServerVoiceChannelById(info.getKey()).get());
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    CompletableFuture
                            .delayedExecutor(Instant.now().until(endInstant, ChronoUnit.SECONDS), TimeUnit.SECONDS)
                            .execute(() -> dataProvider.getServerProvider().unjail(server, user));
                }));
    }

}
