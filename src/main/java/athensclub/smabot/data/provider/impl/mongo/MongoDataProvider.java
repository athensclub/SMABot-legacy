package athensclub.smabot.data.provider.impl.mongo;

import athensclub.smabot.Main;
import athensclub.smabot.data.provider.DataProvider;
import athensclub.smabot.data.provider.ServerProvider;
import athensclub.smabot.data.provider.UserProvider;
import athensclub.smabot.command.Permission;
import athensclub.smabot.player.SimpleSongInfo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * An implementation of {@link athensclub.smabot.data.provider.DataProvider} using MongoDB.
 */
public class MongoDataProvider implements DataProvider {

    private final MongoServerProvider serverProvider;

    private final MongoUserProvider userProvider;

    public MongoDataProvider() {
        MongoClient client = MongoClients.create(Main.BUILD_MODE.getMongoURI());
        MongoDatabase database = client.getDatabase(Main.MONGO_DB_NAME);
        serverProvider = new MongoServerProvider(database);
        userProvider = new MongoUserProvider(database);
    }

    @Override
    public ServerProvider getServerProvider() {
        return serverProvider;
    }

    @Override
    public UserProvider getUserProvider() {
        return userProvider;
    }

    /**
     * Test the implementation of the given {@link MongoDataProvider} instance.
     * This will be a manual test, where the user has to watch the print result to see
     * the correctness of the implementation.
     *
     * @param dataProvider the {@link MongoDataProvider} instance to test.
     */
    public static void test(MongoDataProvider dataProvider) {
        System.out.println("This is a manual test, please inspect the print results carefully.");
        System.out.println("Testing ServerDataProvider");
        System.out.println("BANNED BEFORE BAN " + dataProvider.getServerProvider().getAllBannedSongs("234"));
        System.out.println("IS 'fake uri' BANNED " + dataProvider.getServerProvider().isBanned("234", "fake uri"));
        dataProvider.getServerProvider().ban("234", "fake uri", "fake title");
        System.out.println("BANNED AFTER BAN " + dataProvider.getServerProvider().getAllBannedSongs("234"));
        System.out.println("IS 'fake uri' BANNED " + dataProvider.getServerProvider().isBanned("234", "fake uri"));
        dataProvider.getServerProvider().unban("234", "fake uri");
        System.out.println("BANNED AFTER UNBAN " + dataProvider.getServerProvider().getAllBannedSongs("234"));
        System.out.println("IS 'fake uri' BANNED " + dataProvider.getServerProvider().isBanned("234", "fake uri"));

        System.out.println("PERMISSION BEFORE SET " + dataProvider.getServerProvider().hasPermissionOptional("234", "123", Permission.PERMISSION_ANNOUNCE));
        dataProvider.getServerProvider().setPermission("234", "123", Permission.PERMISSION_ANNOUNCE, true);
        System.out.println("PERMISSION AFTER SET " + dataProvider.getServerProvider().hasPermissionOptional("234", "123", Permission.PERMISSION_ANNOUNCE));

        dataProvider.getServerProvider().toggleAnnounce("234");
        System.out.println("ANNOUNCE");

        System.out.println("PREFIX BEFORE SET " + dataProvider.getServerProvider().getPrefix("234"));
        dataProvider.getServerProvider().setPrefix("234", "yo");
        System.out.println("PREFIX AFTER SET " + dataProvider.getServerProvider().getPrefix("234"));

        System.out.println("Finished testing ServerDataProvider, now testing UserDataProvider");

        System.out.println("BEFORE NEW LIBRARY:");
        System.out.println("LIBRARY COUNT " + dataProvider.getUserProvider().libraryCount("123"));
        System.out.println("LIBRARY GET ALL " + dataProvider.getUserProvider().libraryGetAll("123"));

        dataProvider.getUserProvider().libraryNew("123");
        System.out.println("AFTER NEW LIBRARY:");
        System.out.println("LIBRARY COUNT " + dataProvider.getUserProvider().libraryCount("123"));
        System.out.println("LIBRARY GET ALL " + dataProvider.getUserProvider().libraryGetAll("123"));
        System.out.println("LIBRARY GET AT 1 " + dataProvider.getUserProvider().libraryGet("123", 1));

        dataProvider.getUserProvider().libraryAddSong("123", 1, new SimpleSongInfo("fake title", "fake uri"));
        System.out.println("AFTER ADD FAKE SONG:");
        System.out.println("LIBRARY COUNT " + dataProvider.getUserProvider().libraryCount("123"));
        System.out.println("LIBRARY GET ALL " + dataProvider.getUserProvider().libraryGetAll("123"));
        System.out.println("LIBRARY GET AT 1 " + dataProvider.getUserProvider().libraryGet("123", 1));

        dataProvider.getUserProvider().libraryRemoveRange("123", 1, 1, 1);
        System.out.println("AFTER REMOVE RANGE 1,1:");
        System.out.println("LIBRARY COUNT " + dataProvider.getUserProvider().libraryCount("123"));
        System.out.println("LIBRARY GET ALL " + dataProvider.getUserProvider().libraryGetAll("123"));
        System.out.println("LIBRARY GET AT 1 " + dataProvider.getUserProvider().libraryGet("123", 1));
    }
}
