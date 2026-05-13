package homeTry.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import homeTry.common.cache.CacheType;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        for (CacheType type : CacheType.values()) {
            manager.registerCustomCache(type.getCacheName(),
                    Caffeine.newBuilder()
                            .expireAfterWrite(type.getTtlSeconds(), TimeUnit.SECONDS)
                            .maximumSize(type.getMaxSize())
                            .build());
        }

        return manager;
    }
}
