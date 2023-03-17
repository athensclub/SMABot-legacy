package athensclub.smabot.player;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.api.google.YoutubeManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A class that is responsible for fetching {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack}
 * instances from user input.
 */
public class SongSearcher {

    private static final Predicate<String> urlPattern = Pattern.compile("(?i)\\b((?:https?:(?:/{1,3}|[a-z0-9%])|[a-z0-9.\\-]+[.](?:com|net|org|edu|gov|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|post|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|Ja|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw)/)(?:[^\\s()<>{}\\[\\]]+|\\([^\\s()]*?\\([^\\s()]+\\)[^\\s()]*?\\)|\\([^\\s]+?\\))+(?:\\([^\\s()]*?\\([^\\s()]+\\)[^\\s()]*?\\)|\\([^\\s]+?\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’])|(?:(?<!@)[a-z0-9]+(?:[.\\-][a-z0-9]+)*[.](?:com|net|org|edu|gov|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|post|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|Ja|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw)\\b/?(?!@)))").asPredicate();

    private final YoutubeManager youtubeManager;

    private final AudioPlayerManager playerManager;

    public SongSearcher(YoutubeManager youtubeManager, AudioPlayerManager playerManager) {
        this.youtubeManager = youtubeManager;
        this.playerManager = playerManager;
    }

    /**
     * Load the information of the song from the given search query or song url.
     *
     * @param query the url of the song or the search query
     * @return the {@link SimpleSongInfo} instance.
     */
    /*public SimpleSongInfo loadInfo(String query) {
        if (query.isBlank())
            throw new SMABotUserException("Please enter song url or song name");

        String url = query;
        if (!urlPattern.test(query)) {
            SearchResult searchResult = youtubeManager.search(query);
            url = urlFromID(searchResult.getId().getVideoId());
        }
        return new SimpleSongInfo(searchResult.getSnippet().getTitle(),url);
    }*/

    /**
     * Load a song/playlist from the given query, where if the query is the url, then this will load the song
     * from the given url, otherwise it will search for the song in youtube. The fetched song/playlist will be
     * handled by the given handler.
     *
     * @param query   a url or song name to search for song.
     * @param handler a {@link AudioLoadResultHandler} instance that will handle events involved in loading the song.
     * @return a {@link Future} instance that is running the load task.
     */
    public Future<Void> load(String query, AudioLoadResultHandler handler) {
        if (query.isBlank())
            throw new SMABotUserException("Please enter song url or song name");

        String url = query;
        if (!urlPattern.test(query))
            url = urlFromID(youtubeManager.search(query).getId().getVideoId());
        return playerManager.loadItem(url, handler);
    }

    private String urlFromID(String id) {
        try {
            return "https://www.youtube.com/watch?v=" + URLEncoder.encode(id, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new SMABotUserException("An error occurred while encoding song query!");
        }
    }
}
