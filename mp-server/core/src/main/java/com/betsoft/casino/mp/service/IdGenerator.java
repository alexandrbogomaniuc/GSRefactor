package com.betsoft.casino.mp.service;

import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.ILongIdGenerator;
import com.abs.casino.common.util.ISequencer;
import com.abs.casino.common.util.ISequencerPersister;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: flsh
 * Date: 07.07.2009
 */
@Service
public class IdGenerator implements ILongIdGenerator {
    private static final Logger LOG = LogManager.getLogger(IdGenerator.class);
    //key is class FQN
    private static final ConcurrentMap<String, ISequencer> ids = new ConcurrentHashMap<>();
    private final ISequencerPersister persister;

    public IdGenerator(ISequencerPersister persister) {
        this.persister = persister;
    }

    @PreDestroy
    public synchronized void shutdownBlockAllocators() {
        for (ISequencer sequencer : ids.values()) {
            sequencer.shutdownAllocator();
        }
    }

    @Override
    public long getNext(Class klass) {
        long now = System.currentTimeMillis();
        String name = klass.getCanonicalName();
        ISequencer seq;
        try {
            seq = getSequencer(name);
        } catch (CommonException e) {
            throw new RuntimeException("Cannot get sequencer", e);
        }
        long id = seq.getAndIncrement();
        StatisticsManager.getInstance().updateRequestStatistics("IdGenerator: " + name,
                System.currentTimeMillis() - now);
        return id;
    }

    @Override
    public long getNext(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ISequencer> getAllObjects() {
        return ids;
    }

    @Override
    public void init(ISequencerPersister iSequencerPersister) {
        //nop, init from constructor
    }

    public ISequencer getSequencer(String name) throws CommonException {
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
}
