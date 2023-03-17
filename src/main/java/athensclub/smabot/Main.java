package athensclub.smabot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.function.Supplier;

public class Main {

    public static final BuildMode BUILD_MODE = BuildMode.DEPLOY;

    private static final String DEBUG_TOKEN;
    private static final String DEPLOY_TOKEN;

    private static final String DEPLOY_MONGO_PASSWORD;
    public static final String MONGO_DB_NAME = "smabot";

    static {
        String setDebugToken = null;
        String setDeployToken = null;
        String setMongoPassword = null;
        try (BufferedReader debugBr = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("discord_token_debug.txt"))));
             BufferedReader deployBr = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("discord_token.txt"))));
             BufferedReader mongoBr = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("mongodb_password.txt"))))) {
            setDebugToken = debugBr.readLine();
            setDeployToken = deployBr.readLine();
            setMongoPassword = mongoBr.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DEPLOY_TOKEN = setDeployToken;
        DEBUG_TOKEN = setDebugToken;
        DEPLOY_MONGO_PASSWORD = setMongoPassword;
    }

    public static void main(String[] args) throws Exception {
        new SMABot().start(BUILD_MODE.getDiscordToken());
    }

    public enum BuildMode {
        DEBUG(() -> DEBUG_TOKEN, () -> "mongodb://localhost:27017", "%"),
        DEPLOY(() -> DEPLOY_TOKEN,
                () -> "mongodb+srv://smabot:" + DEPLOY_MONGO_PASSWORD +
                        "@smabot.9zd3q.mongodb.net/" + MONGO_DB_NAME
                        + "?retryWrites=true&w=majority",
                "!");

        private final Supplier<String> discordToken; // probably should do lazy eval like this for consistency.

        private final Supplier<String> mongoURI;

        public final String prefix;

        BuildMode(Supplier<String> discordToken, Supplier<String> mongoURI, String prefix) {
            this.discordToken = discordToken;
            this.mongoURI = mongoURI;
            this.prefix = prefix;
        }

        public String getDiscordToken() {
            return discordToken.get();
        }

        public String getMongoURI() {
            return mongoURI.get();
        }

    }

}
