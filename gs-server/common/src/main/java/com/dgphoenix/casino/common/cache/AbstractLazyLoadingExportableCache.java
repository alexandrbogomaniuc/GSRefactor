package com.dgphoenix.casino.common.cache;

import com.dgphoenix.casino.cassandra.persist.ILazyLoadingPersister;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.persist.StreamPersister;
import com.dgphoenix.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.web.statistics.IStatisticsGetter;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by grien on 06.03.15.
 */
public abstract class AbstractLazyLoadingExportableCache<KEY, VALUE extends IDistributedCacheEntry> extends AbstractExportableCache<VALUE>
        implements ILoadingCache {
    private ILazyLoadingPersister<KEY, VALUE> persister;
    protected LoadingCache<KEY, VALUE> cache;

    public void init(ILazyLoadingPersister<KEY, VALUE> persister, int maxCacheSize) {
        this.persister = persister;
        initCache(maxCacheSize);

        StatisticsManager.getInstance().registerStatisticsGetter(this.getClass().getSimpleName() + " statistics", new IStatisticsGetter() {
            @Override
            public String getStatistics() {
                return "size=" + AbstractLazyLoadingExportableCache.this.cache.size() +
                        ", stats=" + AbstractLazyLoadingExportableCache.this.cache.stats();
            }
        });
    }

    /**
     * Override if require specific processing of loaded value
     *
     * @param maxCacheSize
     */
    protected void initCache(int maxCacheSize) {
        CacheLoader<KEY, VALUE> defaultLoader = new CacheLoader<KEY, VALUE>() {
            @Override
            public VALUE load(KEY key) throws Exception {
                return AbstractLazyLoadingExportableCache.this.persister.get(key);
            }
        };
        this.cache = createCache(maxCacheSize, defaultLoader);
    }

    protected LoadingCache<KEY, VALUE> createCache(int maxCacheSize, CacheLoader<KEY, VALUE> loader) {
        return CacheBuilder.newBuilder()
                .maximumSize(maxCacheSize)
                .recordStats()
                .concurrencyLevel(8)
                .build(loader);
    }

    @Override
    public void put(VALUE entry) throws CommonException {
        put(getKey(entry), entry);
    }

    public void put(KEY key, VALUE entry) {
        cache.put(key, entry);
    }

    protected void delete(KEY key, VALUE entry) {
        cache.invalidate(key);
        persister.delete(key, entry);
    }

    @Override
    public VALUE getObject(String id) {
        return cache.getIfPresent(parseKeyFromString(id));
    }

    @Override
    public Map<KEY, VALUE> getAllObjects() {
        return ImmutableMap.copyOf(cache.asMap());
    }

    public VALUE get(KEY id) {
        try {
            return cache.get(id);
        } catch (ExecutionException e) {
            return null;
        }
    }

    @Override
    public int size() {
        long size = cache.size();
        return size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
    }

    @Override
    public void exportEntries(final ObjectOutputStream outStream) throws IOException {
        StreamPersister<KEY, VALUE> streamPersister = (StreamPersister<KEY, VALUE>) this.persister;
        streamPersister.processAll(new TableProcessor<Pair<KEY, VALUE>>() {
            @Override
            public void process(Pair<KEY, VALUE> entry) throws IOException {
                outStream.writeObject(new ExportableCacheEntry(String.valueOf(entry.getKey()), entry.getValue()));
            }
        });
    }

    @Override
    public void exportEntries(final ObjectOutputStream outStream, Long bankId) throws IOException {
        StreamPersister<KEY, VALUE> streamPersister = (StreamPersister<KEY, VALUE>) this.persister;
        streamPersister.processByCondition(new TableProcessor<Pair<KEY, VALUE>>() {
            @Override
            public void process(Pair<KEY, VALUE> entry) throws IOException {
                outStream.writeObject(new ExportableCacheEntry(String.valueOf(entry.getKey()), entry.getValue()));
            }
        }, "byBank", bankId);
    }

    @Override
    public void invalidate(String key) {
        cache.invalidate(parseKeyFromString(key));
    }

    protected KEY parseKeyFromString(String key) {
        return (KEY) key;
    }

    protected KEY getKey(VALUE value) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
