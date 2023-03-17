package athensclub.smabot.data.provider;

public interface DataProvider {

    /**
     * Get the {@link ServerProvider} instance managing server data.
     *
     * @return the {@link ServerProvider} instance managing server data.
     */
    ServerProvider getServerProvider();

    /**
     * Get the {@link UserProvider} instance managing user data.
     *
     * @return the {@link UserProvider} instance managing user data.
     */
    UserProvider getUserProvider();

}
