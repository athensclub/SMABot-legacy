package athensclub.smabot.data.provider;

import athensclub.smabot.command.Permission;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.Map;
import java.util.Optional;

/**
 * A data provider that manages server data.
 */
public interface ServerProvider {

    /**
     * Ban the song from giver the server.
     *
     * @param server the snowflake id of the server to ban the song.
     * @param uri    the URI of the song to be banned.
     * @param title  the title of the song to be banned.
     */
    void ban(String server, String uri, String title);

    /**
     * Unban the song that is banned from the given server.
     *
     * @param server the snowflake id of the server to unban the song.
     * @param uri    the URI of the song to be unbanned.
     */
    void unban(String server, String uri);

    /**
     * Check whether the song with the given uri is banned in the given server.
     *
     * @param server the server to check.
     * @param uri    the uri of the song to check.
     * @return whether the song with the given uri is banned in the given server.
     */
    boolean isBanned(String server, String uri);

    /**
     * Get an immutable map of the mapping from the banned song uri to its title.
     *
     * @param server the snowflake id of the server to get the banned songs list.
     * @return an immutable map of the mapping from the banned song uri to its title.
     */
    Map<String, String> getAllBannedSongs(String server);

    /**
     * Check whether the user has the given permission.
     *
     * @param server the snowflake id of the server to check.
     * @param user   the snowflake id of the user to check.
     * @param perm   the permission to check.
     * @return {@link Optional} of boolean that is {@code true} when the user has the permission,
     * otherwise false, and the {@link Optional} will be empty if the permission has not been explicitly set
     * to the user in the given server.
     */
    Optional<Boolean> hasPermissionOptional(String server, String user, Permission perm);

    /**
     * Check whether the given user has the given permission. If the permission for the given
     * user is not yet specified, the default value for the permission will be returned.
     *
     * @param server the server to check.
     * @param user   the user to check.
     * @param perm   the permission to check.
     * @return {@code true} if the given user has the given permission, otherwise {@code false}
     * or the default value for the permission, if the given user doesn't have the permission
     * specified.
     */
    default boolean hasPermission(Server server, User user, Permission perm) {
        return hasPermissionOptional(server.getIdAsString(), user.getIdAsString(), perm)
                .orElse(perm.defaultPermissionFor(user, server));
    }

    /**
     * Set the permission with the given name for the given user in the given server to be
     * the given value.
     *
     * @param server the snowflake id of the server to set the permission.
     * @param user   the snowflake id of the user to set the permission.
     * @param perm   the permission to set.
     * @param value  the value of whether the user should have the permission.
     */
    void setPermission(String server, String user, Permission perm, boolean value);

    /**
     * Check whether the given server has the announce property (The property that make the bot messages
     * the currently playing song).
     *
     * @param server the snowflake id of the server to check.
     * @return whether the server has the announce property. {@code true} if the bot will announce in the
     * server, otherwise {@code false}.
     */
    boolean isAnnounce(String server);

    /**
     * Toggle whether the server should has the announce property (The property that make the bot messages
     * the currently playing song).
     *
     * @param server the snowflake id of the server to set.
     */
    void toggleAnnounce(String server);

    /**
     * Get the {@link Optional} of prefix for this bot in the given server.
     *
     * @param server the snowflake id of the server to get the prefix.
     * @return the optional of prefix for this bot in the given server. Will be empty if the
     * prefix has not been explicitly set yet.
     */
    Optional<String> getPrefix(String server);

    /**
     * Set the prefix for this bot in the given server.
     *
     * @param server the snowflake id of the server to set the prefix.
     * @param prefix the prefix to set to.
     */
    void setPrefix(String server, String prefix);

    /**
     * Check whether this bot is listening to the given text channel in the given server. Default value is {@code true}.
     *
     * @param server      the snowflake id of the server to check.
     * @param textChannel the snowflake id of the text channel to check.
     * @return whether this bot is listening to the given text channel in the given server.
     */
    boolean isListeningTo(String server, String textChannel);

    /**
     * Set whether this bot would listen to the given text channel in the given server.
     *
     * @param server      the snowflake id of the server to set.
     * @param textChannel the snowflake id of the text channel to set.
     * @param listening   whether this bot will listen to the channel or not.
     */
    void setListeningTo(String server, String textChannel, boolean listening);

    /**
     * Put the given user with the given id in the server with the given id in jail.
     * When a user who is being jail enter voice channel, they will be automatically moved
     * to the jail channel specified for them. The user who is being jailed will be set free
     * after the endTime has passed.
     *
     * @param server  the snowflake id of the server to jail the user.
     * @param user    the snowflake id of the user to be jailed.
     * @param channel the snowflake id of the voice channel to be the jail for the user.
     * @param endTime the ISO instant format of the time that the user will be set free.
     */
    void jail(String server, String user, String channel, String endTime);

    /**
     * Remove the user with the given id in the server with the given id from the jail.
     *
     * @param server the snowflake id of the server to unjail the user.
     * @param user   the snowflake id of the server to unjail.
     */
    void unjail(String server, String user);

    /**
     * Return a mapping from a jailed user to an entry where the key element is the jail channel
     * name and the value element is ISO instant time for when the user will be set free.
     *
     * @param server the snowflake id of the server to get all jailed user from.
     * @return a mapping of all jailed user in the given user.
     */
    Map<String, Map.Entry<String, String>> getAllJailed(String server);

    /**
     * Return a mapping from a user id to a mapping from a server id to an entry where the key element is
     * the jail channel for the user and the value element is the ISO instant end time for the user jail.
     * This will includes all user that has interacted with this bot's database (if they are in jail)
     * and include every server that has interacted with this bot's database
     * (if they contains user being jailed)
     *
     * @return a mapping from user id to a mapping from server id to entry of channel and endtime.
     */
    Map<String, Map<String, Map.Entry<String, String>>> getAllJailed();

    /**
     * If the user with the given id is being put in jail in the server with the given id,
     * this will return an optional with the value of the id of the channel that the user is
     * being jailed to, otherwise this will return an empty optional.
     *
     * @param server the snowflake id of the server to get the jail channel from.
     * @param user   the snowflake id of the user to get the jail channel from.
     * @return the id optional of the channel that the user is being jailed to, if the user is jailed,
     * otherwise none.
     */
    Optional<String> jailChannel(String server, String user);

    /**
     * If the user with the given id is being put in jail in the server with the given id,
     * this will return optional with the value of ISO instant time format string, representing the
     * time that the user will be set free from jail, otherwise this will return an empty optional.
     *
     * @param server the snowflake id of the server to get the end time from.
     * @param user   the snowflake id of the user to get the end time from.
     * @return the time optional when the user will be set free from jail, if the user is jailed,
     * otherwise none.
     */
    Optional<String> jailEndTime(String server, String user);

}
