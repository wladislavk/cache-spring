package cache;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class CacheController {
    private CacheItems cacheItems;
    private CacheConfiguration cacheConfiguration;
    private ElementAdditionHandler elementAdditionHandler;
    private VacantKeyFinder vacantKeyFinder;

    public CacheController(
            CacheItems cacheItems,
            CacheConfiguration cacheConfiguration,
            ElementAdditionHandler elementAdditionHandler,
            VacantKeyFinder vacantKeyFinder
    ) {
        this.cacheItems = cacheItems;
        this.cacheConfiguration = cacheConfiguration;
        this.elementAdditionHandler = elementAdditionHandler;
        this.vacantKeyFinder = vacantKeyFinder;
    }

    @GetMapping("/object/{key}")
    public CacheItemResponse show(@PathVariable("key") int key) {
        CacheItem item = cacheItems.get(key);
        if (item == null || item.isExpired()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return CacheItemResponse.fromCacheItem(item);
    }

    @PostMapping("/object")
    public CacheItemResponse insert(
            @RequestParam("ttl") Optional<Integer> ttl,
            @RequestBody CachePutRequest cachePutRequest
    ) {
        CacheItem item = this.elementAdditionHandler.addElement(cachePutRequest.getId(), ttl);
        int key = this.vacantKeyFinder.findVacantKey();
        if (key == -1) {
            throw new ResponseStatusException(HttpStatus.INSUFFICIENT_STORAGE);
        }
        this.cacheItems.put(key, item);
        return CacheItemResponse.fromCacheItem(item);
    }

    @PutMapping("/object/{key}")
    public CacheItemResponse update(
            @PathVariable("key") int key,
            @RequestParam("ttl") Optional<Integer> ttl,
            @RequestBody CachePutRequest cachePutRequest
    ) {
        if (key < 0 || key >= this.cacheConfiguration.getNumberOfSlots()) {
            throw new ResponseStatusException(HttpStatus.INSUFFICIENT_STORAGE);
        }
        CacheItem item = this.elementAdditionHandler.addElement(cachePutRequest.getId(), ttl);
        this.cacheItems.put(key, item);
        return CacheItemResponse.fromCacheItem(item);
    }

    @DeleteMapping("/object/{key}")
    public void destroy(@PathVariable("key") int key) {
        CacheItem item = cacheItems.get(key);
        if (item == null || item.isExpired()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        cacheItems.delete(key);
    }
}
