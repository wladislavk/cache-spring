package cache;

public class CacheItemResponse {
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CacheItemResponse fromCacheItem(CacheItem item) {
        CacheItemResponse response = new CacheItemResponse();
        response.id = item.getId();
        return response;
    }
}
