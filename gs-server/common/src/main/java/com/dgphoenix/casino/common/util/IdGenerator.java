/**
 * User: flsh
 * Date: 07.07.2009
 */
package com.abs.casino.common.util;

import com.abs.casino.common.cache.AbstractExportableCache;
import com.abs.casino.common.cache.CacheKeyInfo;
import com.abs.casino.common.cache.ExportableCacheEntry;
import com.abs.casino.common.util.ILongIdGenerator;
import com.abs.casino.common.util.ISequencer;
import com.abs.casino.common.util.ISequencerPersister;
import com.abs.casino.common.util.Sequencer;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.persist.StreamPersister;
import com.abs.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@com.abs.casino.common.cache.CacheKeyInfo(description = "key is class FQN")
public class IdGenerator extends AbstractExportableCache<Sequencer> implements ILongIdGenerator {
    private static final Logger LOG = LogManager.getLogger(IdGenerator.class);

    private static final IdGenerator instance = new IdGenerator();
    //key is class FQN
    private static final ConcurrentMap<String, ISequencer> ids = new ConcurrentHashMap<>();
    private ISequencerPersister persister;

    public static IdGenerator getInstance() {
        return instance;
    }

    private IdGenerator() {
    }

    @Override
    public synchronized void init(ISequencerPersister persister) {
        if (this.persister != null) {
            LOG.warn("SequencerPersister already initialized");
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
                    if (existSequencer != null) {
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
