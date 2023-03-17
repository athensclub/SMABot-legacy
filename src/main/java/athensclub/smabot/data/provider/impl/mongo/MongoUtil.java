package athensclub.smabot.data.provider.impl.mongo;

import com.mongodb.client.model.UpdateOptions;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

/**
 * A utility class for interacting with MongoDB.
 */
public final class MongoUtil {

    /**
     * An {@link UpdateOptions} instance that include upsert and only upsert.
     */
    public static final UpdateOptions UPSERT_OPTION = new UpdateOptions().upsert(true);

    private MongoUtil() {
    }

    /**
     * Return a filter that match the document that has the same snowflake as the
     * given snowflake. In other words, return a filter that match a document that
     * has the property 'snowflake' equal to the given snowflake.
     *
     * @param snowflake the snow flake to match.
     * @return a filter that match the document that has the same snowflake as the
     * given snowflake.
     */
    public static Bson snowflakeEq(String snowflake) {
        return eq("snowflake", snowflake);
    }
}
