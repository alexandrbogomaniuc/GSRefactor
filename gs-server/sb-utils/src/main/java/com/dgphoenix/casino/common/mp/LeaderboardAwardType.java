package com.dgphoenix.casino.common.mp;

import java.util.HashMap;
import java.util.Map;

public enum LeaderboardAwardType {
    MONEY(1), SPECIAL(2), XP(3);

    private final int id;

    static final Map<Integer, LeaderboardAwardType> idsMap = new HashMap<Integer, LeaderboardAwardType>();
    static {
        for (LeaderboardAwardType value : LeaderboardAwardType.values()) {
            idsMap.put(value.getId(), value);
        }
    }

    LeaderboardAwardType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LeaderboardAwardType getById(int id) {
        return idsMap.get(id);
    }
}
