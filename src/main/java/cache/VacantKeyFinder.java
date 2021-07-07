package cache;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class VacantKeyFinder {
    private CacheItems cacheItems;
    private CacheConfiguration cacheConfiguration;

    public VacantKeyFinder(CacheItems cacheItems, CacheConfiguration cacheConfiguration) {
        this.cacheItems = cacheItems;
        this.cacheConfiguration = cacheConfiguration;
    }

    public int findVacantKey() {
        HashMap<Integer, String> items = this.cacheItems.getAll();
        for (int i = 0; i < this.cacheConfiguration.getNumberOfSlots(); i++) {
            if (!items.containsKey(i)) {
                return i;
            }
        }
        if (this.cacheConfiguration.getEvictionPolicy() == CacheConfiguration.EvictionPolicies.OLDEST_FIRST) {
            return this.findOldest(items);
        }
        if (this.cacheConfiguration.getEvictionPolicy() == CacheConfiguration.EvictionPolicies.NEWEST_FIRST) {
            return this.findNewest(items);
        }
        return -1;
    }

    private int findOldest(HashMap<Integer, String> items) {
        LocalDateTime oldest = null;
        int oldestKey = -1;
        for (Integer key : items.keySet()) {
            CacheItem cacheItem = CacheItem.fromJson(items.get(key));
            if (oldest == null || cacheItem.getCreatedAt().isBefore(oldest)) {
                oldest = cacheItem.getCreatedAt();
                oldestKey = key;
            }
        }
        return oldestKey;
    }

    private int findNewest(HashMap<Integer, String> items) {
        LocalDateTime newest = null;
        int newestKey = -1;
        for (Integer key : items.keySet()) {
            CacheItem cacheItem = CacheItem.fromJson(items.get(key));
            if (newest == null || cacheItem.getCreatedAt().isAfter(newest)) {
                newest = cacheItem.getCreatedAt();
                newestKey = key;
            }
        }
        return newestKey;
    }
}
