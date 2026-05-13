package homeTry.common.cache;

public enum CacheType {

    DIARY("diary", 30, 500),
    DIARY_HISTORY("diary-history", 600, 1000);

    private final String cacheName;
    private final int ttlSeconds;
    private final int maxSize;

    CacheType(String cacheName, int ttlSeconds, int maxSize) {
        this.cacheName = cacheName;
        this.ttlSeconds = ttlSeconds;
        this.maxSize = maxSize;
    }

    public String getCacheName() {
        return cacheName;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public static final class Constants {
        public static final String DIARY = "diary";
        public static final String DIARY_HISTORY = "diary-history";

        private Constants() {
        }
    }
}
