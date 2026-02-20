package com.dgphoenix.casino.common.lock;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 04.02.15.
 */
public interface ILockManager {
    LockingInfo lock(String id) throws CommonException;

    boolean unlock(String id, long lockTime) throws CommonException;

    LockingInfo lock(String id, final long timeout) throws CommonException;

    LockingInfo tryLock(String id) throws CommonException;

    boolean isLockOwner(String id);

    void unlock(LockingInfo lockInfo);
}
