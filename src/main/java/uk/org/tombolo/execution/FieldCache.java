package uk.org.tombolo.execution;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;

/**
 * Class for caching field values when exporting. This will speed up exports that re-use calculated fields.
 */
public class FieldCache {
    private Logger log = LoggerFactory.getLogger(FieldCache.class);
    private static final int LOGGING_FREQUENCY = 1000;
    private static final int DEFAULT_CACHE_SIZE_ENTRIES = 100000;
    private Cache<String,String> fieldCache;

    int hits = 0;
    int misses = 0;


    public FieldCache(){
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("fields",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(DEFAULT_CACHE_SIZE_ENTRIES)))
                .build();
        cacheManager.init();
        fieldCache = cacheManager.getCache("fields", String.class, String.class);
    }

    public String getChachedValue(Field field, Subject subject){
        String cachedValue = fieldCache.get(getCacheKey(field, subject));
        if (cachedValue != null)
            hits++;
        else
            misses++;
        if ((hits+misses) % LOGGING_FREQUENCY == 0)
            log.info("Caching milestone {} ({} hits) ({} misses)", hits+misses, hits, misses);
        return cachedValue;
    }

    public void putCachedValue(Field field, Subject subject, String value){
        fieldCache.put(getCacheKey(field, subject),value);
    }

    private String getCacheKey(Field field, Subject subject){
        // FIXME: This function will mess things up if there are in one export two fields of the same class with the same label
        // FIXME: Each field instance should offer a signature method, capturing its parameters
        return field.getClass().getName()
                +"\t"
                + field.getLabel()
                + "\t"
                +subject.getId();
    }
}