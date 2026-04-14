package vel.vn;

import vel.vn.resp.RespValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage {

    private final Map<String, RespValue> storage;

    private static final InMemoryStorage INSTANCE = new InMemoryStorage();

    private InMemoryStorage() {
        storage = new ConcurrentHashMap<>();
    }

    public static InMemoryStorage getInstance() {
        return INSTANCE;
    }

    public RespValue get(String key) {
        return storage.get(key);
    }

    public void put(String key, RespValue value) {
        storage.put(key, value);
    }
}
