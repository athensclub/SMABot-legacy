package athensclub.smabot.data.provider;

import athensclub.smabot.player.Library;
import athensclub.smabot.player.SimpleSongInfo;

import java.util.List;

/**
 * A data provider that manages user data.
 */
public interface UserProvider {

    /**
     * Get the number of libraries the user with the given id has.
     *
     * @param id the snowflake of the user to get the count from.
     * @return the number of libraries the user with the given id has.
     */
    int libraryCount(String id);

    /**
     * Remove ranges of song from a library with the given index of a user with the given id with
     * the begin and end index using the given value.
     *
     * @param id    the snowflake id of the user to remove ranges of song from.
     * @param index the index of the library to remove ranges of song from (1-indexed).
     * @param begin the begin index of the range to remove the song (1-indexed, inclusive).
     * @param end   the end index of the range to remove the song (1-indexed, inclusive).
     * @return a list of songs that were removed.
     */
    List<SimpleSongInfo> libraryRemoveRange(String id, int index, int begin, int end);

    /**
     * Add the given song to the library with the given index of the user with the given id.
     *
     * @param id    the snowflake id of the user to add the song to.
     * @param index the index of the library to add the song to (1-indexed).
     * @param song  the song to add.
     */
    void libraryAddSong(String id, int index, SimpleSongInfo song);

    /**
     * Get the library with the given index of the user with the given id.
     *
     * @param id    the snowflake id of the user to get the songs from.
     * @param index the index of the library to get the songs from (1-indexed).
     * @return as library with the given index of the user with the given id.
     */
    Library libraryGet(String id, int index);

    /**
     * Create a new empty library and append it to the end of the user with the given id's
     * library list.
     *
     * @param id the snowflake id of the user to add the library to.
     */
    void libraryNew(String id);

    /**
     * Get all the library in the list of libraries of the user with the given id (immutable
     * version backed by the original version).
     *
     * @param id the snowflake id of the user to get the libraries from.
     * @return all the libraries of the user with the given id
     */
    List<Library> libraryGetAll(String id);

    /**
     * Set the visibility of the library of the user with the given id. That is, whether the library
     * contents will be visible to other users or not.
     *
     * @param id        the snowflake id of the user to set the visibility of library from.
     * @param index     the library index.
     * @param isPrivate whether the library is going to private.
     */
    void librarySetVisibility(String id, int index, boolean isPrivate);

}
