package com.betsoft.casino.mp.data.service;

import com.betsoft.casino.mp.data.persister.LockPersister;
import com.abs.casino.cassandra.IRemoteUnlocker;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.lock.ILockManager;
import com.dgphoenix.casino.common.lock.LockingInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * User: flsh
 * Date: 10.04.18.
 */
@Service
public class LockService implements ILockManager {
    private static final Logger LOG = LogManager.getLogger(LockService.class);

    private final LockPersister lockPersister;
    private final ClusterInfoService clusterInfoService;
    private final IRemoteUnlocker remoteUnlocker;

    public LockService(LockPersister lockPersister, ClusterInfoService clusterInfoService,
                       IRemoteUnlocker remoteUnlocker) {
        this.lockPersister = lockPersister;
        this.clusterInfoService = clusterInfoService;
        this.remoteUnlocker = remoteUnlocker;
    }

    @PostConstruct
    public void init() {
        lockPersister.setLoadBalancer(clusterInfoService);
        lockPersister.setRemoteUnlocker(remoteUnlocker);
        LOG.debug("init: initialized");
    }

    @Override
    public LockingInfo lock(String id) throws CommonException {
        return lockPersister.lock(id);
    }

    @Override
    public LockingInfo lock(String id, final long timeout) throws CommonException {
        return lockPersister.lock(id, timeout);
    }

    @Override
    public LockingInfo tryLock(String id) throws CommonException {
        return lockPersister.tryLock(id);
    }

    @Override
    public boolean isLockOwner(String id) {
        return lockPersister.isLockOwner(id);
    }

    @Override
    public void unlock(LockingInfo lockInfo) {
        lockPersister.unlock(lockInfo);
    }

    @Override
    public boolean unlock(String id, long lockTime) throws CommonException {
        return lockPersister.unlock(id, lockTime);
    }
}
