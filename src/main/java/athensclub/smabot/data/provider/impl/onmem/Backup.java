package athensclub.smabot.data.provider.impl.onmem;

import athensclub.smabot.api.google.GoogleDriveManager;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A class responsible for storing backup file of on-memory provider to Google Drive.
 * This will automatically perform backup every 12 hours.
 */
public class Backup {

    public static final Logger LOG = Logger.getLogger(Backup.class.getName());

    private final Gson gson;

    private final GoogleDriveManager drive;

    private final OnMemoryDataProvider provider;

    private final ScheduledExecutorService scheduler;

    private String fileID;

    public Backup(OnMemoryDataProvider provider, GoogleDriveManager drive) {
        this.drive = drive;
        this.provider = provider;
        gson = new Gson();

        LOG.info("Attempting to find currently existing file in Google Drive.");
        findFileID();
        if (fileID == null) {
            LOG.info("Failed to find existing file in Google Drive. Creating new file...");
            File folder = drive.create("smabot", "application/vnd.google-apps.folder");
            File file = drive.create("smabot_data.json", "application/json", List.of(folder.getId()));
            fileID = file.getId();
            LOG.info("Created new file with id: " + fileID);
            upload();
        } else {
            LOG.info("Found file with id: " + fileID);
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::upload, 12, 12, TimeUnit.HOURS);
    }

    /**
     * Upload the backup data to Google Drive.
     */
    public void upload() {
        LOG.info("Uploading backup to Google Drive...");
        drive.update(fileID, new InputStreamContent("application/json", new ByteArrayInputStream(serialize().getBytes())));
        LOG.info("Backup upload complete.");
    }

    /**
     * Load the backup data from Google Drive as {@link BackupData} instance.
     *
     * @return the backup data from Google Drive as {@link BackupData} instance.
     */
    public BackupData load() {
        LOG.info("Downloading backup from Google Drive...");
        BackupData data = deserialize(drive.get(fileID));
        LOG.info("Backup download complete.");
        return data;
    }

    private String serialize() {
        return gson.toJson(new BackupData(provider));
    }

    private BackupData deserialize(String json) {
        return gson.fromJson(json, BackupData.class);
    }

    private void findFileID() {
        String pageToken = null;
        do {
            FileList folders = drive.search("mimeType='application/vnd.google-apps.folder' and name='smabot'", pageToken);
            for (File folder : folders.getFiles()) {
                String pageToken2 = null;
                do {
                    FileList files = drive.search("'" + folder.getId() + "' in parents and mimeType='application/json' and name='smabot_data.json'", pageToken2);
                    Optional<String> id = files.getFiles().stream().map(File::getId).filter(Objects::nonNull).findAny();
                    if (id.isPresent())
                        fileID = id.get();
                    pageToken2 = files.getNextPageToken();
                } while (fileID == null && pageToken2 != null);
            }
            pageToken = folders.getNextPageToken();
        } while (fileID == null && pageToken != null);
    }

}
