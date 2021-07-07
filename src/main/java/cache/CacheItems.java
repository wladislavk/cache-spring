package cache;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class CacheItems {
    private HashMap<Integer, String> items;

    public CacheItems() {
        this.items = new HashMap<>();
    }

    public HashMap<Integer, String> getAll() {
        return this.items;
    }

    public CacheItem get(int key) {
        String jsonItem = items.get(key);
        return CacheItem.fromJson(jsonItem);
    }

    public void put(int key, CacheItem value) {
        String jsonValue = value.toJson();
        this.items.put(key, jsonValue);
    }

    public void delete(int key) {
        this.items.remove(key);
    }

    public void clear() {
        this.items.clear();
    }
}
