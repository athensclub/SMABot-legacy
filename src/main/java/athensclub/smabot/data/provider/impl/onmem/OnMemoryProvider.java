package athensclub.smabot.data.provider.impl.onmem;

import athensclub.smabot.SMABotUtil;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * A base class for on-memory providers implementation.
 */
public class OnMemoryProvider<T> implements Serializable {

    private final ConcurrentMap<String, T> data;

    public OnMemoryProvider() {
        data = new ConcurrentHashMap<>();
    }

    /**
     * Extract the data from the map of snowflake to the data type. Will create new instance of data type
     * using the given supplier if the map does not contain the data with the given id key.
     *
     * @param id      the snowflake to get data from the map.
     * @param builder a {@link Supplier} that will create a new instance of data when the data does not exist yet.
     * @return a data instance of the given id.
     */
    public T getData(String id, Supplier<T> builder) {
        return SMABotUtil.getOrCreateAtomic(data, id, builder);
    }

    /**
     * Get the underlying data structure of this provider.
     *
     * @return the underlying data structure of this provider.
     */
    protected ConcurrentMap<String, T> get() {
        return data;
    }

    @Override
    public String toString() {
        return "OnMemoryProvider{" +
                "data=" + data +
                '}';
    }
}
