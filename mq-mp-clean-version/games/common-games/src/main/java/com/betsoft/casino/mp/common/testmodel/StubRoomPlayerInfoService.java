package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.service.IRoomPlayerInfoService;
import com.betsoft.casino.mp.service.IRoomTaskCreator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: flsh
 * Date: 20.02.2020.
 */
public class StubRoomPlayerInfoService<S extends ISeat> implements IRoomPlayerInfoService {
    private final List<S> seats;
    private transient ReentrantLock lock;

    public StubRoomPlayerInfoService(List<S> seats) {
        this.seats = seats;
        this.lock = new ReentrantLock();
    }

    @Override
    public IRoomPlayerInfo get(long accountId) {
        Optional<S> first = seats.stream().filter(seat -> seat.getPlayerInfo().getId() == accountId).findFirst();
        return first.get().getPlayerInfo();
    }

    @Override
    public boolean hasPlayersWithPendingOperation(long roomId) {
        return false;
    }

    @Override
    public Collection<IRoomPlayerInfo> getForRoom(long roomId) {
        return Collections.emptyList();
    }

    @Override
    public void put(IRoomPlayerInfo playerInfo) {

    }

    @Override
    public void remove(IRoomTaskCreator roomTaskCreator, long roomId, long accountId) {

    }

    @Override
    public void lock(long accountId) {
        lock.lock();
    }

    @Override
    public boolean tryLock(long accountId, long time, TimeUnit timeunit) {
        return lock.tryLock();
    }

    @Override
    public void unlock(long accountId) {
        lock.unlock();
    }

    @Override
    public boolean isLocked(long accountId) {
        return lock.isLocked();
    }

    @Override
    public void forceUnlock(long accountId) {
        lock.unlock();
    }
}
