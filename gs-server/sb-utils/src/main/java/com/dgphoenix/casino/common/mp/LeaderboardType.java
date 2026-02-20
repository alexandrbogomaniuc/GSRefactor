package com.dgphoenix.casino.common.mp;

import java.util.HashMap;
import java.util.Map;

public enum LeaderboardType {
    NORMAL(1), TICKETED_DRAW(2);

    private int code;

    private static final Map<Integer, LeaderboardType> idsMap = new HashMap<Integer, LeaderboardType>();

    static {
        for (LeaderboardType type : values()) {
            idsMap.put(type.code, type);
        }
    }

    LeaderboardType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static LeaderboardType valueOf(int code) {
        return idsMap.get(code);
    }
}
