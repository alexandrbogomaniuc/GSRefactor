package com.dgphoenix.casino.common.lock;

import com.abs.casino.common.lock.LocalLockInfo;

/**
 * User: flsh
 * Date: 03.02.15.
 */
public class LockingInfo {
    private LocalLockInfo localLockInfo;
    private ServerLockInfo serverLockInfo;

    public LockingInfo(LocalLockInfo localLockInfo, ServerLockInfo serverLockInfo) {
        this.localLockInfo = localLockInfo;
        this.serverLockInfo = serverLockInfo;
    }

    public LocalLockInfo getLocalLockInfo() {
        return localLockInfo;
    }

    public void setLocalLockInfo(LocalLockInfo localLockInfo) {
        this.localLockInfo = localLockInfo;
    }

    public ServerLockInfo getServerLockInfo() {
        return serverLockInfo;
    }

    public void setServerLockInfo(ServerLockInfo serverLockInfo) {
        this.serverLockInfo = serverLockInfo;
    }

    public String getLockId() {
        return serverLockInfo.getLockId();
    }

    @Override
    public String toString() {
        return "LockingInfo[" +
                "localLockInfo=" + localLockInfo +
                ", serverLockInfo=" + serverLockInfo +
                ']';
    }
}
