package vel.vn;

import vel.vn.resp.RespValue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage {

    private final Map<String, RespValue> storage;

    private final Map<String, LocalDateTime> expiryMap;

    private static final InMemoryStorage INSTANCE = new InMemoryStorage();

    private InMemoryStorage() {
        storage = new ConcurrentHashMap<>();
        expiryMap = new ConcurrentHashMap<>();
    }

    public static InMemoryStorage getInstance() {
        return INSTANCE;
    }

    public RespValue get(String key) {
        if (expiryMap.containsKey(key) && expiryMap.get(key).isBefore(LocalDateTime.now())) {
            expiryMap.remove(key);
            storage.remove(key);
            return RespValue.NULL;
        }
        return storage.getOrDefault(key, RespValue.NULL);
    }

    public void put(String key, RespValue value, Long expireTime) {
        storage.put(key, value);
        if (expireTime != null) {
            expiryMap.put(key, LocalDateTime.now().plus(expireTime, ChronoUnit.MILLIS));
        }
    }
}
