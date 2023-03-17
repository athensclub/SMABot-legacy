package athensclub.smabot.api.google;

import athensclub.smabot.SMABotUserException;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class GoogleDriveManager {

    private static final List<String> SCOPES = List.of(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_METADATA);

    private Drive service;

    public GoogleDriveManager() {
        try {
            InputStream in = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("smabot-google-drive-backup-credentials.json"));
            GoogleCredentials serviceAccountCredentials = ServiceAccountCredentials.fromStream(in)
                    .createScoped(SCOPES);
            HttpRequestInitializer credentials = new HttpCredentialsAdapter(serviceAccountCredentials);
            service = new Drive.Builder(GoogleAPIUtil.HTTP_TRANSPORT, GoogleAPIUtil.JSON_FACTORY, credentials)
                    .setApplicationName(GoogleAPIUtil.APPLICATION_NAME)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Download the content from the file with the given id.
     *
     * @param id the id of the file to download from.
     * @return the content of the file.
     */
    public String get(String id) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            service.files().get(id)
                    .executeMediaAndDownloadTo(stream);
            return stream.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SMABotUserException("An error occurred while download file from Google Drive.");
        }
    }

    /**
     * Update the file with the given id with the new given content.
     *
     * @param id      the id of the file to update the content.
     * @param content the content to update the file to.
     */
    public void update(String id, AbstractInputStreamContent content) {
        try {
            service.files()
                    .update(id, new File().setMimeType("application/json"), content)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SMABotUserException("An error occurred while updating file in Google Drive.");
        }
    }

    /**
     * Create a new file with the given name and mimetype, with the given parents.
     *
     * @param name     the name of the file to create.
     * @param mimeType the mime-type of the file to create.
     * @param parents  a list of id of parent directories of the file to create.
     * @return the created {@link File} instance.
     */
    public File create(String name, String mimeType, List<String> parents) {
        try {
            return service.files().create(new File()
                    .setName(name)
                    .setMimeType(mimeType)
                    .setParents(parents))
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SMABotUserException("An error occurred while attempting to create new file in Google Drive");
        }
    }

    /**
     * Create a new file with the given name and mime-type.
     *
     * @param name     the name of the file to be created.
     * @param mimeType the mime-type of the file to be created.
     * @return the created {@link File} instance.
     */
    public File create(String name, String mimeType) {
        try {
            return service.files().create(new File()
                    .setName(name)
                    .setMimeType(mimeType))
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SMABotUserException("An error occurred while attempting to create new file in Google Drive");
        }
    }

    /**
     * Search for the file that match with the given q query, as specified in Google Drive documentation.
     *
     * @param q         the Q query, as specified in Google Drive documentation.
     * @param pageToken the page token for continuing previous list of pages, {@code null} if the search is at the start of the list.
     * @return a {@link FileList} instance that is obtained by executing the search.
     * @see <a href="https://developers.google.com/drive/api/v3/search-files">https://developers.google.com/drive/api/v3/search-files</a>
     */
    public FileList search(String q, String pageToken) {
        try {
            return service.files().list()
                    .setQ(q)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SMABotUserException("An error occurred while attempting to fetch files from Google Drive.");
        }
    }

}
