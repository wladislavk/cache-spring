package cache;

import com.google.gson.Gson;

import java.time.LocalDateTime;

public class CacheItem {
    private int id;
    private LocalDateTime createdAt;
    private int ttl = 0;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getTtl() {
        return ttl;
    }

    private LocalDateTime getExpiryDate() {
        return this.createdAt.plusSeconds(this.ttl);
    }

    public boolean isExpired() {
        if (this.ttl == 0) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return this.getExpiryDate().isBefore(now);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static CacheItem fromJson(String json) {
        return new Gson().fromJson(json, CacheItem.class);
    }
}
