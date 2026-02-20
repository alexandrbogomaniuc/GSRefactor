/**
 * User: flsh
 * Date: 07.07.2009
 */
package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.cache.AbstractExportableCache;
import com.dgphoenix.casino.common.cache.CacheKeyInfo;
import com.dgphoenix.casino.common.cache.ExportableCacheEntry;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.persist.StreamPersister;
import com.dgphoenix.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@CacheKeyInfo(description = "key is class FQN")
public class IntegerIdGenerator extends AbstractExportableCache<IntegerSequencer> implements IIntegerIdGenerator {
    private static final Logger LOG = LogManager.getLogger(IntegerIdGenerator.class);
    private static final IntegerIdGenerator instance = new IntegerIdGenerator();
    //key is class FQN
    private static final ConcurrentMap<String, IIntegerSequencer> ids = new ConcurrentHashMap<>();
    private IIntegerSequencerPersister persister;

    public static IntegerIdGenerator getInstance() {
        return instance;
    }

    private IntegerIdGenerator() {
    }

    @Override
    public synchronized void init(IIntegerSequencerPersister persister) {
        if (this.persister != null) {
            LOG.warn("SequencerPersister already initialized");
        }
        this.persister = persister;
    }

    @Override
    public synchronized void shutdownBlockAllocators() {
        assertInitialized();
        for (IIntegerSequencer sequencer : ids.values()) {
            sequencer.shutdownAllocator();
        }
    }

    public IIntegerSequencer getSequencer(String name) throws CommonException {
        assertInitialized();
        IIntegerSequencer sequencer = ids.get(name);
        if (sequencer == null) {
            synchronized (ids) {
                sequencer = ids.get(name);
                if (sequencer == null) {
                    sequencer = persister.getOrCreateSequencer(name);
                    if (sequencer == null) {
                        LOG.error("Sequencer after put is null: '" + name + "'");
                        throw new RuntimeException("sequencer is null");
                    }
                    sequencer.init();
                    IIntegerSequencer existSequencer = ids.putIfAbsent(name, sequencer);
                    if (existSequencer != null) {
                        sequencer = existSequencer;
                    }
                }
            }
        }
        return sequencer;
    }

    @Override
    public int getNext(Class klass) {
        String name = klass.getCanonicalName();
        return getNext(name);
    }

    @Override
    public int getNext(String sequencerName) {
        long now = System.currentTimeMillis();
        IIntegerSequencer seq;
        try {
            seq = getSequencer(sequencerName);
        } catch (CommonException e) {
            throw new RuntimeException("Cannot get sequencer", e);
        }
        int id = seq.getAndIncrement();
        StatisticsManager.getInstance().updateRequestStatistics("IdGenerator: " + sequencerName, System.currentTimeMillis() - now);
        return id;
    }

    public void setNext(Class klass, int value) throws CommonException {
        String name = klass.getCanonicalName();
        IIntegerSequencer seq = getSequencer(name);
        seq.setValue(value);
    }

    @Override
    public void put(IntegerSequencer entry) {
        throw new UnsupportedOperationException("Cannot put Sequencer entry to cache");
    }

    @Override
    public void exportEntries(final ObjectOutputStream outStream) throws IOException {
        assertInitialized();
        StreamPersister<String, ISequencer> streamPersister = (StreamPersister<String, ISequencer>) persister;
        streamPersister.processAll(new TableProcessor<Pair<String, ISequencer>>() {
            @Override
            public void process(Pair<String, ISequencer> entry) throws IOException {
                outStream.writeObject(new ExportableCacheEntry(entry.getKey(), entry.getValue()));
            }
        });
    }

    @Override
    public void importEntry(ExportableCacheEntry entry) {
        assertInitialized();
        synchronized (ids) {
            IIntegerSequencer seq = ids.get(entry.getKey());
            if (seq != null) {
                LOG.warn("importEntry: sequencer already exist: " + entry);
                throw new RuntimeException("importEntry: sequencer already exist: " + entry);
            }
            final IIntegerSequencer sequencer = (IIntegerSequencer) entry.getValue();
            final IIntegerSequencer saved;
            try {
                saved = persister.importSequencer(sequencer);
            } catch (CommonException e) {
                throw new RuntimeException("Import error", e);
            }
            LOG.info("importEntry: imported=" + sequencer + ", saved=" + saved);
        }
    }

    @Override
    public int size() {
        return ids.size();
    }

    @Override
    public Object getObject(String id) {
        return ids.get(id);
    }

    @Override
    public Map<String, IIntegerSequencer> getAllObjects() {
        return ids;
    }

    @Override
    public String getAdditionalInfo() {
        return NO_INFO;
    }

    @Override
    public String printDebug() {
        return "ids.size()=" + ids.size();
    }

    @Override
    public boolean isRequiredForImport() {
        return true;
    }

    private void assertInitialized() {
        if (persister == null) {
            throw new RuntimeException("Not initialized");
        }
    }
}
