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
public class IntegerIdGenerator extends AbstractExportableCache<Sequencer> implements IIntegerIdGenerator {
    private static final Logger LOG = Logger.getLogger(IntegerIdGenerator.class);

    private static final IntegerIdGenerator instance = new IntegerIdGenerator();
    //key is class FQN
    private static final ConcurrentMap<String, IIntegerSequencer> ids = new ConcurrentHashMap<String, IIntegerSequencer>();
    private IIntegerSequencerPersister persister;

    public static IntegerIdGenerator getInstance() {
        return instance;
    }

    private IntegerIdGenerator() {
    }

    @Override
    public synchronized void init(IIntegerSequencerPersister persister) {
        if(this.persister != null) {
            throw new RuntimeException("IIntegerSequencerPersister already initialized");
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
                    if(existSequencer != null) {
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
        StatisticsManager.getInstance().updateRequestStatistics("IntegerIdGenerator: " + sequencerName,
                System.currentTimeMillis() - now);
        return id;
    }

    public void setNext(Class klass, int value) throws CommonException {
        String name = klass.getCanonicalName();
        IIntegerSequencer seq = getSequencer(name);
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
            Collection<Map.Entry<String, IIntegerSequencer>> entries = ids.entrySet();
            for (Map.Entry<String, IIntegerSequencer> entry : entries) {
                outStream.writeObject(new ExportableCacheEntry(entry.getKey(), entry.getValue()));
            }
        }
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
