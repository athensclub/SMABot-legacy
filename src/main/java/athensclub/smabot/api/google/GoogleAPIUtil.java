package athensclub.smabot.api.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * A utility class for interacting with Google API.
 */
public final class GoogleAPIUtil {

    public static final String APPLICATION_NAME = "athensclub-smabot/1.0.0";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static final NetHttpTransport HTTP_TRANSPORT;

    static {
        NetHttpTransport netHttpTransport = null;
        try {
            netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HTTP_TRANSPORT = netHttpTransport;
    }

    private GoogleAPIUtil(){}

}
