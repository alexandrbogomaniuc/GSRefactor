package com.dgphoenix.casino.common.mp;

import java.util.HashMap;
import java.util.Map;

public enum LeaderboardStatus {
    SCHEDULED(1),
    STARTED(2),
    FINISHED(3),
    PROCESSED(4);

    private int code;

    private static final Map<Integer, LeaderboardStatus> idsMap = new HashMap<Integer, LeaderboardStatus>();

    static {
        for (LeaderboardStatus status : values()) {
            idsMap.put(status.code, status);
        }
    }

    LeaderboardStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static LeaderboardStatus valueOf(int code) {
        return idsMap.get(code);
    }
}
