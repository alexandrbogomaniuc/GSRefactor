package com.betsoft.casino.mp.piratescommon.model.math;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class EnemyData {
    private final int[] levels;
    private final Map<Integer, WeaponData[]> weaponDataMap;

    public EnemyData(int[] levels, Map<Integer, WeaponData[]> weaponDataMap) {
        this.levels = levels;
        this.weaponDataMap = weaponDataMap;
    }


    public Double getSwAvgPayouts(int weaponId, int levelId, double payMultiplier,Map<Long, Double> hitPointMapData) {
        WeaponData[] weaponData = weaponDataMap.get(weaponId);
        int level = levelId >= weaponData.length ? 0 : levelId;
        WeaponData data = weaponData[level];
        Map<Integer, Double> criticalHitProb = data.getCriticalHitProb();
        double instantKillEV = data.getInstantKillEV();
        double totaEvWithoutIK = 0;

        double chSum = criticalHitProb.values().stream().mapToDouble(Double::doubleValue).sum() / 100;
        AtomicReference<Double> sumMult = new AtomicReference<>((double) 0);
        criticalHitProb.forEach((integer, aDouble) -> sumMult.updateAndGet(v -> (double) (v + (integer * aDouble / 100))));
        for (Map.Entry<Long, Double> longDoubleEntry : hitPointMapData.entrySet()) {
            Long HP = longDoubleEntry.getKey();
            Double prob = longDoubleEntry.getValue();
            double evCurrent = HP * payMultiplier * prob * (1 - chSum + sumMult.get());
            totaEvWithoutIK += evCurrent;
        }
        return (totaEvWithoutIK + instantKillEV / 100) * 100;
    }

    public int[] getLevels() {
        return levels;
    }

    public WeaponData getWeaponDataMap(int weaponId, int levelId) {
        WeaponData[] weaponData = weaponDataMap.get(weaponId);
        return weaponData[levelId > weaponData.length - 1 ? 0 : levelId];
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyData{");
        sb.append(", levels=").append(Arrays.toString(levels));
        sb.append(", weaponDataMap=").append(weaponDataMap);
        sb.append('}');
        return sb.toString();
    }
}
