package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class RemoteUnlockRequest implements KafkaRequest {
    private String lockManagerName;
    private String lockId;
    private long lockTime;

    public RemoteUnlockRequest() {}

    public RemoteUnlockRequest(String lockManagerName, String lockId, long lockTime) {
        this.lockManagerName = lockManagerName;
        this.lockId = lockId;
        this.lockTime = lockTime;
    }

    public String getLockManagerName() {
        return lockManagerName;
    }

    public String getLockId() {
        return lockId;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockManagerName(String lockManagerName) {
        this.lockManagerName = lockManagerName;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }
}
