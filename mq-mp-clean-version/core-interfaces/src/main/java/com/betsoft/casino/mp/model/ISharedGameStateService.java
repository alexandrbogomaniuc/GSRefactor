package com.betsoft.casino.mp.model;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 26.02.2022.
 */
public interface ISharedGameStateService {
    void put(ISharedGameState state);

    <T extends ISharedGameState> T get(long roomId, Class<T> type);

    void remove(long roomId);

    Collection<ISharedGameState> getAll();

    void lock(long roomId);

    boolean tryLock(long roomId, long time, TimeUnit timeunit) throws InterruptedException;

    void unlock(long roomId);

    boolean isLocked(long roomId);

    void forceUnlock(long roomId);
}
