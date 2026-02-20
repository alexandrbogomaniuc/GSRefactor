package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IRoomPlayerInfo;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 10.11.17.
 */
public interface IRoomPlayerInfoService {
    IRoomPlayerInfo get(long accountId);

    boolean hasPlayersWithPendingOperation(long roomId);

    Collection<IRoomPlayerInfo> getForRoom(long roomId);

    void put(IRoomPlayerInfo playerInfo);

    void remove(IRoomTaskCreator roomTaskCreator, long roomId, long accountId);

    void lock(long accountId);

    boolean tryLock(long accountId, long time, TimeUnit timeunit) throws InterruptedException;

    void unlock(long accountId);

    boolean isLocked(long accountId);

    void forceUnlock(long accountId);
}
