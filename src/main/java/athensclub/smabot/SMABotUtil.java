package athensclub.smabot;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


/**
 * Contains all utility methods used by SMABot.
 */
public final class SMABotUtil {

    private SMABotUtil() {
    }

    /**
     * Get the value of the map from the given key. If the key exists, this will return the value, otherwise
     * this will create a new value using a given builder and put in the map atomically using
     * {@link ConcurrentMap#putIfAbsent(Object, Object)}
     *
     * @param map     the map to get the value from.
     * @param key     the key of the map to get the value from.
     * @param builder the builder to use for creating new value when the key does not exist.
     * @param <K>     the key type of the map.
     * @param <V>     the value type of the map.
     * @return the value of the map from the given key or the result of the builder if the key does not exist.
     */
    public static <K, V> V getOrCreateAtomic(ConcurrentMap<K, V> map, K key, Supplier<V> builder) {
        if (!map.containsKey(key))
            map.putIfAbsent(key, builder.get());
        return map.get(key);
    }

    /**
     * Get the {@link User} instance from the given mention and the api. This will
     * throw {@link SMABotUserException} when it fails to find user.
     *
     * @param mention the discord mention tag for the user to find.
     * @param api  the {@link DiscordApi} instance to find the user.
     * @return the {@link User} instance from the given mention and the guild.
     */
    public static User getMemberFromMention(String mention, DiscordApi api) {
        String id = mention.trim().substring(2, mention.length() - 1);
        if (id.startsWith("!"))
            id = id.substring(1);
        SMABotUserException[] toThrow = new SMABotUserException[1];
        try {
            User result = api.getUserById(id).exceptionally(t -> {
                toThrow[0] = new SMABotUserException("Can not find user with id: " + mention);
                return null;
            }).get();
            if(toThrow[0] != null)
                throw toThrow[0];
            return result;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new SMABotUserException("An error occurred while finding user with id: " + mention);
        }
    }

    /**
     * Run the given runnable, then if the exception is thrown, if it is {@link SMABotUserException}
     * then it will send error message to the given message channel, otherwise the stack trace will
     * be logged.
     *
     * @param r       the task to be run.
     * @param channel the message channel to send error message to.
     */
    public static void runWithException(Runnable r, TextChannel channel) {
        //TODO: Implement using Javacord
        try {
            r.run();
        } catch (SMABotUserException e) {
            new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("error")
                            .setDescription(":x: " + e.getMessage())
                            .setColor(Color.RED))
                    .send(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
