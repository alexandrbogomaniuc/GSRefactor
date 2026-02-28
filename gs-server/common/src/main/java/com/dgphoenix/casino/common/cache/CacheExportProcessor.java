package com.dgphoenix.casino.common.cache;

import com.abs.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.util.Pair;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 18.01.16
 */
public class CacheExportProcessor<T extends IDistributedCacheEntry> implements TableProcessor<Pair<String, T>> {

    private final ObjectOutputStream outStream;

    public CacheExportProcessor(ObjectOutputStream outStream) {
        this.outStream = outStream;
    }

    @Override
    public void process(Pair<String, T> entry) throws IOException {
        outStream.writeObject(new ExportableCacheEntry(entry.getKey(), entry.getValue()));
    }

}
