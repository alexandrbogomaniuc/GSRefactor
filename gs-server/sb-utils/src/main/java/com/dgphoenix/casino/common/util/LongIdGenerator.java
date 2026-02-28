package com.abs.casino.common.util;

import com.dgphoenix.casino.common.cache.AbstractExportableCache;
import com.dgphoenix.casino.common.cache.ExportableCacheEntry;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: flsh
 * Date: 19.03.13
 */
public class LongIdGenerator extends AbstractExportableCache<Sequencer> implements ILongIdGenerator {
    private static final Logger LOG = Logger.getLogger(LongIdGenerator.class);

    private static final LongIdGenerator instance = new LongIdGenerator();
    //key is class FQN
    private final static ConcurrentMap<String, ISequencer> ids = new ConcurrentHashMap();
    private ISequencerPersister persister;

    public static LongIdGenerator getInstance() {
        return instance;
    }

    private LongIdGenerator() {
    }

    @Override
    public synchronized void init(ISequencerPersister persister) {
        if(this.persister != null) {
            throw new RuntimeException("SequencerPersister already initialized");
        }
        this.persister = persister;
    }

    @Override
    public synchronized void shutdownBlockAllocators() {
        assertInitialized();
        for (ISequencer sequencer : ids.values()) {
            sequencer.shutdownAllocator();
        }
    }

    public ISequencer getSequencer(String name) throws CommonException {
        assertInitialized();
        ISequencer sequencer = ids.get(name);
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
                    ISequencer existSequencer = ids.putIfAbsent(name, sequencer);
                    if(existSequencer != null) {
                        sequencer = existSequencer;
                    }
                }
            }
        }
        return sequencer;
    }

    @Override
    public long getNext(Class klass) {
        String name = klass.getCanonicalName();
        return getNext(name);
    }

    @Override
    public long getNext(String sequencerName) {
        long now = System.currentTimeMillis();
        ISequencer seq;
        try {
            seq = getSequencer(sequencerName);
        } catch (CommonException e) {
            throw new RuntimeException("Cannot get sequencer", e);
        }
        long id = seq.getAndIncrement();
        StatisticsManager.getInstance().updateRequestStatistics("IdGenerator: " + sequencerName, System.currentTimeMillis() - now);
        return id;
    }

    public void setNext(Class klass, long value) throws CommonException {
        String name = klass.getCanonicalName();
        ISequencer seq = getSequencer(name);
        seq.setValue(value);
    }

    @Override
    public void put(Sequencer entry) {
        throw new UnsupportedOperationException("Cannot put Sequencer entry to cache");
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        assertInitialized();
        synchronized (ids) {
            Collection<Map.Entry<String, ISequencer>> entries = ids.entrySet();
            for (Map.Entry<String, ISequencer> entry : entries) {
                outStream.writeObject(new ExportableCacheEntry(entry.getKey(), entry.getValue()));
            }
        }
    }

    @Override
    public void importEntry(ExportableCacheEntry entry) {
        assertInitialized();
        synchronized (ids) {
            ISequencer seq = ids.get(entry.getKey());
            if (seq != null) {
                LOG.warn("importEntry: sequencer already exist: " + entry);
                throw new RuntimeException("importEntry: sequencer already exist: " + entry);
            }
            final ISequencer sequencer = (ISequencer) entry.getValue();
            final ISequencer saved;
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
    public Map<String, ISequencer> getAllObjects() {
        return (Map) ids;
    }

    @Override
    public String getAdditionalInfo() {
        return NO_INFO;
    }

    @Override
    public String printDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("ids.size()=").append(ids.size());
        return sb.toString();
    }

    @Override
    public boolean isRequiredForImport() {
        return true;
    }

    private void assertInitialized() {
        if(persister == null) {
            throw new RuntimeException("Not initialized");
        }
    }

}
