package com.betsoft.casino.mp.missionamazon.model.math;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

public enum BossType {
    SPIDER_QUEEN(1, 1000, 2000),
    STONE_GUARDIAN(2, 2200, 4200),
    PRIMAL_KING(3, 1000, 2500);

    private static Map<Integer, BossType> skinIdsMap = Maps.newHashMap();
    private int skinId;
    private long invulnerabilityTime;
    private long spawnTime;

    static {
        Arrays.stream(values()).forEach(bossType -> skinIdsMap.put(bossType.getSkinId(), bossType));
    }

    BossType(int skinId, long invulnerabilityTime, long spawnTime) {
        this.skinId = skinId;
        this.invulnerabilityTime = invulnerabilityTime;
        this.spawnTime = spawnTime;
    }

    public int getSkinId() {
        return skinId;
    }

    public long getInvulnerabilityTime() {
        return invulnerabilityTime;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public static BossType getBySkinId(int skinId) {
        return skinIdsMap.getOrDefault(skinId, SPIDER_QUEEN);
    }
}
