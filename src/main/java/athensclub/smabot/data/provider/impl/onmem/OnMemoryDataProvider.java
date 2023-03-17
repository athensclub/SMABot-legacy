package athensclub.smabot.data.provider.impl.onmem;

import athensclub.smabot.api.google.GoogleDriveManager;
import athensclub.smabot.data.provider.DataProvider;

/**
 * An implementation of {@link athensclub.smabot.data.provider.DataProvider} using plain java
 * {@link java.util.Map} to handle data.
 */
public class OnMemoryDataProvider implements DataProvider {

    private final OnMemoryServerProvider serverProvider;

    private final OnMemoryUserProvider userProvider;

    private final Backup backup;

    private final GoogleDriveManager drive;

    public OnMemoryDataProvider() {
        drive = new GoogleDriveManager();
        backup = new Backup(this, drive);
        BackupData data = backup.load();
        serverProvider = data.getServerProvider();
        userProvider = data.getUserProvider();
    }

    @Override
    public OnMemoryServerProvider getServerProvider() {
        return serverProvider;
    }

    @Override
    public OnMemoryUserProvider getUserProvider() {
        return userProvider;
    }

}
