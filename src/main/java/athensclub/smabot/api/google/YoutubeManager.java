package athensclub.smabot.api.google;

import athensclub.smabot.SMABotUserException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class YoutubeManager {

    private YouTube youtube;

    private String token;

    public YoutubeManager() {
        InputStream in;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("youtube_data_api_token.txt")))) {
            token = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        youtube = new YouTube.Builder(GoogleAPIUtil.HTTP_TRANSPORT, GoogleAPIUtil.JSON_FACTORY, req -> {
        })
                .setApplicationName(GoogleAPIUtil.APPLICATION_NAME).build();
    }

    /**
     * Search the given query on youtube and return the search result as the most recommended
     * result given by youtube.
     *
     * @param query the query to be searched.
     * @return a search result of the query.
     */
    public SearchResult search(String query) {
        try {
            SearchListResponse result = youtube.search().list("id,snippet")
                    .setKey(token)
                    .setQ(query)
                    .setType("video")
                    .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                    .setMaxResults(1L)
                    .execute();
            if (result.getItems().isEmpty())
                throw new SMABotUserException("Song not found!");
            return result.getItems().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new SMABotUserException("An error occurred while searching for song!");
    }

}
