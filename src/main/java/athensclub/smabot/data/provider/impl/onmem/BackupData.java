package athensclub.smabot.data.provider.impl.onmem;

import java.io.Serializable;

/**
 * Made this class because if we use {@link OnMemoryDataProvider} to serialize data,
 * the default constructor will create new instance of {@link Backup} which will attempt to
 * deserialize again, causing infinite loop. This class will only contain the data, and not
 * any helper objects.
 */
public class BackupData implements Serializable {

    private final OnMemoryUserProvider userProvider;

    private final OnMemoryServerProvider serverProvider;

    //constructor for json serialization (jackson)
    private BackupData() {
        //create placeholder to avoid nulls when the backup is empty.
        userProvider = new OnMemoryUserProvider();
        serverProvider = new OnMemoryServerProvider();
    }

    public BackupData(OnMemoryDataProvider provider) {
        this(provider.getUserProvider(), provider.getServerProvider());
    }

    public BackupData(OnMemoryUserProvider userProvider, OnMemoryServerProvider serverProvider) {
        this.userProvider = userProvider;
        this.serverProvider = serverProvider;
    }

    public OnMemoryServerProvider getServerProvider() {
        return serverProvider;
    }

    public OnMemoryUserProvider getUserProvider() {
        return userProvider;
    }

    @Override
    public String toString() {
        return "BackupData{" +
                "userProvider=" + userProvider +
                ", serverProvider=" + serverProvider +
                '}';
    }
}
