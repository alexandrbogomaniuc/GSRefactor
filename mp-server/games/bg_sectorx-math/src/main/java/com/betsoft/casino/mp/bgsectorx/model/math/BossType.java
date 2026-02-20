package com.betsoft.casino.mp.bgsectorx.model.math;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

public enum BossType {
    BOSS_1(1, 1000, 2000),
    BOSS_2(2, 2200, 4200),
    BOSS_3(3, 1000, 2500),
    BOSS_4(4, 1000, 2500);

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
        return skinIdsMap.getOrDefault(skinId, BOSS_1);
    }
}

