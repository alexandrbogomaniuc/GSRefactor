package com.dgphoenix.casino.common.lock;

/**
 * User: flsh
 * Date: 19.03.13
 */
public interface ChangeLockListener {
    void lockChanged(ServerLockInfo lockInfo);
}
