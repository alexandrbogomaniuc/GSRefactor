package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ISharedGameState;
import com.betsoft.casino.mp.model.ISharedGameStateService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Service
public class SharedGameStateService implements ISharedGameStateService {
    private static final Logger LOG = LogManager.getLogger(SharedGameStateService.class);
    public static final String STATE_STORE = "sharedGameStateStore";
    private final HazelcastInstance hazelcast;
    //key is roomId
    private IMap<Long, ISharedGameState> states;

    public SharedGameStateService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    @PostConstruct
    private void init() {
        states = hazelcast.getMap(STATE_STORE);
        LOG.info("init: completed");
    }

    @Override
    public void put(ISharedGameState state) {
      //  LOG.debug("SharedGameStateService put, roomId: {}, state: {}", state.getRoomId(), state);
        states.set(state.getRoomId(), state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ISharedGameState> T get(long roomId, Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return (T) states.get(roomId);
    }

    @Override
    public void remove(long roomId) {
        LOG.debug("remove: {}", roomId);
        states.delete(roomId);
    }

    @Override
    public Collection<ISharedGameState> getAll() {
        return states.values();
    }

    @Override
    public void lock(long roomId) {
        states.lock(roomId);
    }

    @Override
    public boolean tryLock(long roomId, long time, TimeUnit timeunit) throws InterruptedException {
        return states.tryLock(roomId, time, timeunit);
    }

    @Override
    public void unlock(long roomId) {
        states.unlock(roomId);
    }

    @Override
    public boolean isLocked(long roomId) {
        return states.isLocked(roomId);
    }

    @Override
    public void forceUnlock(long roomId) {
        LOG.debug("HC forceUnlock: {}", roomId);
        states.forceUnlock(roomId);
    }

    @Override
    public String toString() {
        return "SharedGameStateService [" + "states.size=" + states.size() + ']';
    }
}
