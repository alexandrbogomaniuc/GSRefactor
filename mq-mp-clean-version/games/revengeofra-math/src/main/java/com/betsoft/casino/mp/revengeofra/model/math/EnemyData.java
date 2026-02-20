package com.betsoft.casino.mp.revengeofra.model.math;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class EnemyData {
    private final int[] levels;
    private final Map<Integer, WeaponData[]> freeWeaponDataMap;
    private final Map<Integer, WeaponData[]> paidWeaponDataMap;

    public EnemyData(int[] levels, Map<Integer, WeaponData[]> freeWeaponDataMap , Map<Integer, WeaponData[]> paidWeaponDataMap) {
        this.levels = levels;
        this.freeWeaponDataMap = freeWeaponDataMap;
        this.paidWeaponDataMap = paidWeaponDataMap;
    }


    public Double getSwAvgPayouts(int weaponId, int levelId, double payMultiplier, boolean paidMode) {
        WeaponData[] weaponData = paidMode && paidWeaponDataMap.get(weaponId) != null ?
                paidWeaponDataMap.get(weaponId) :
                freeWeaponDataMap.get(weaponId);

        int level = levelId >= weaponData.length ? 0 : levelId;
        WeaponData data = weaponData[level];
        Map<Integer, Double> criticalHitProb = data.getCriticalHitProb();
        double instanceKillEV = data.getInstanceKillEV();
        double totaEvWithoutIK = 0;

        double chSum = criticalHitProb.values().stream().mapToDouble(Double::doubleValue).sum() / 100;
        AtomicReference<Double> sumMult = new AtomicReference<>((double) 0);
        criticalHitProb.forEach((integer, aDouble) -> sumMult.updateAndGet(v -> (v + (integer * aDouble / 100))));
        for (Map.Entry<Long, Double> longDoubleEntry : data.getEnemyHiHitPointMap().entrySet()) {
            Long HP = longDoubleEntry.getKey();
            Double prob = longDoubleEntry.getValue();
            double evCurrent = HP * payMultiplier * prob * (1 - chSum + sumMult.get());
            totaEvWithoutIK += evCurrent;
        }
        return (totaEvWithoutIK + instanceKillEV / 100) * 100;
    }

    public int[] getLevels() {
        return levels;
    }

    public WeaponData getWeaponDataMap(int weaponId, int levelId, boolean paidMode) {
        WeaponData[] weaponData = paidMode && paidWeaponDataMap.get(weaponId) != null ?
                paidWeaponDataMap.get(weaponId) :
                freeWeaponDataMap.get(weaponId);
        return weaponData[levelId > weaponData.length - 1 ? 0 : levelId];
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyData{");
        sb.append(", levels=").append(Arrays.toString(levels));
        sb.append(", freeWeaponDataMap=").append(freeWeaponDataMap);
        sb.append(", paidWeaponDataMap=").append(paidWeaponDataMap);
        sb.append('}');
        return sb.toString();
    }
}
