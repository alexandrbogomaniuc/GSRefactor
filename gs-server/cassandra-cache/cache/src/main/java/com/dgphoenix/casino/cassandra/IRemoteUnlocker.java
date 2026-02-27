package com.abs.casino.cassandra;

import com.dgphoenix.casino.common.lock.ILockManager;

public interface IRemoteUnlocker {

    boolean unlock(int serverId, Class<? extends ILockManager> lockManagerClass, String lockId, long lockTime);
}
