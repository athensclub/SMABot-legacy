package athensclub.smabot.command.handler.server;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.command.CommandScanner;
import athensclub.smabot.command.ServerCommandData;
import athensclub.smabot.command.handler.CommandHandler;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class JailCommandHandler implements CommandHandler<ServerCommandData> {

    private final Map<String, Double> unitToSeconds = new HashMap<>() {{
        put("day", 86400.0);
        put("days", 86400.0);
        put("hour", 3600.0);
        put("hours", 3600.0);
        put("minute", 60.0);
        put("minutes", 60.0);
        put("second", 1.0);
        put("seconds", 1.0);
    }};

    @Override
    public void handle(ServerCommandData data) {
        CommandScanner sc = data.scanner();
        if (!sc.hasNext()) {
            Map<String, Map.Entry<String, String>> allJailed = data.getServerProvider()
                    .getAllJailed(data.getServer().getIdAsString());
            StringBuilder message = new StringBuilder();
            message.append("People who are currently jailed are the following:");
            if (!allJailed.isEmpty()) {
                allJailed.forEach((user, info) ->
                {
                    try {
                        message.append("\n")
                                .append(data.getApi().getUserById(user).get().getNicknameMentionTag())
                                .append(" is in a jail named '")
                                .append(data.getApi().getServerVoiceChannelById(info.getKey()).get().getName())
                                .append("' until ")
                                .append(Instant.parse(info.getValue()).atOffset(ZoneOffset.ofHours(7)).format(DateTimeFormatter.RFC_1123_DATE_TIME));
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                message.append(" none\n");
            }

            new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle("Jail")
                    .setDescription(message.toString())
                    .setColor(Color.GREEN))
                    .send(data.getTextChannel());
            return;
        }

        User target = sc.next().asUser();
        if (data.getServer().isOwner(target))
            throw new SMABotUserException("Owner can not be jailed.");
        if (data.getServer().isAdmin(target) && data.getServer().isOwner(data.getUser()))
            throw new SMABotUserException("You must be owner to send admins to jail.");

        String channelID = sc.next().getValue();
        Optional<ServerVoiceChannel> channelOptional = data.getServer().getVoiceChannelById(channelID);
        if (channelOptional.isEmpty())
            throw new SMABotUserException("The voice channel with id: " + channelID + " does not exists!");

        double duration = sc.next().asDouble();
        if (duration < 0)
            throw new SMABotUserException("Duration of the jail time must be > 0!");

        String unitsRaw = sc.next().getValue();
        String units = unitsRaw.trim().toLowerCase();
        if (!unitToSeconds.containsKey(units))
            throw new SMABotUserException(unitsRaw + " is not a valid time unit!");

        long seconds = (long) (duration * unitToSeconds.get(units));
        Instant endTime = Instant.now().plusSeconds(seconds);
        data.getServerProvider().jail(data.getServer().getIdAsString(), target.getIdAsString(), channelID, endTime.toString());
        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS).execute(() -> data.getServerProvider().unjail(data.getServer().getIdAsString(), target.getIdAsString()));

        if (target.getConnectedVoiceChannel(data.getServer()).isPresent())
            target.move(channelOptional.get());

        new MessageBuilder().setEmbed(new EmbedBuilder()
                .setTitle("Jail")
                .setDescription("Successfully jailed " + target.getNicknameMentionTag() + " in " +
                        channelOptional.get().getName() + " until " +
                        endTime.atOffset(ZoneOffset.ofHours(7)).format(DateTimeFormatter.RFC_1123_DATE_TIME))
                .setColor(Color.GREEN))
                .send(data.getTextChannel());
    }

}
