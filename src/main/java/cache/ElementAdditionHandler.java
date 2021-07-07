package cache;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ElementAdditionHandler {
    private CacheConfiguration cacheConfiguration;

    public ElementAdditionHandler(CacheConfiguration cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    public CacheItem addElement(int id, Optional<Integer> ttl) {
        CacheItem item = new CacheItem();
        item.setId(id);
        item.setCreatedAt(LocalDateTime.now());
        if (ttl.isPresent()) {
            item.setTtl(ttl.get());
        } else {
            item.setTtl(this.cacheConfiguration.getDefaultTtl());
        }
        return item;
    }
}
