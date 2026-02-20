package com.betsoft.casino.mp.clashofthegods.model.math.enemies;

import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.Triple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AbstractEnemyData implements IEnemyData{
    Map<Integer, WeaponData[]> freeWeaponData;
    Map<Integer, WeaponData[]> paidWeaponData;
    int[] levels;
    Map<Integer, Pair<Double, Double>> weaponDropData;
    HashMap<Integer, List<Triple<Integer, Integer, Double>>> additionalWeaponKilledTable;

    public AbstractEnemyData() {
        weaponDropData = new HashMap<>();
        additionalWeaponKilledTable = new HashMap<>();
        freeWeaponData = new HashMap<>();
        paidWeaponData = new HashMap<>();
    }

    public Map<Integer, WeaponData[]> getFreeWeaponData() {
        return freeWeaponData;
    }

    public Map<Integer, WeaponData[]> getPaidWeaponData() {
        return paidWeaponData;
    }

    public Map<Integer, Pair<Double, Double>> getWeaponDropData() {
        return weaponDropData;
    }

    @Override
    public HashMap<Integer, List<Triple<Integer, Integer, Double>>> getAdditionalWeaponKilledTable() {
        return additionalWeaponKilledTable;
    }

    public WeaponData getWeaponDataMap(int weaponId, int levelId, boolean paidMode) {
        WeaponData[] weaponData = paidMode && paidWeaponData.get(weaponId) != null ?
                paidWeaponData.get(weaponId) :
                freeWeaponData.get(weaponId);
        return weaponData[levelId > weaponData.length - 1 ? 0 : levelId];
    }

    public Double getSwAvgPayouts(int weaponId, int levelId, double payMultiplier, boolean paidMode,
                                  Map<Long, Double> hitPointMapData) {
        WeaponData[] weaponData = paidMode && paidWeaponData.get(weaponId) != null ?
                paidWeaponData.get(weaponId) :
                freeWeaponData.get(weaponId);

        int level = levelId >= weaponData.length ? 0 : levelId;
        WeaponData data = weaponData[level];
        Map<Integer, Double> criticalHitProb = data.getCriticalHitProb();
        double instanceKillEV = data.getInstanceKillEV();
        double totaEvWithoutIK = 0;

        double chSum = criticalHitProb.values().stream().mapToDouble(Double::doubleValue).sum() / 100;
        AtomicReference<Double> sumMult = new AtomicReference<>((double) 0);
        criticalHitProb.forEach((integer, aDouble) -> sumMult.updateAndGet(v -> (v + (integer * aDouble / 100))));
        for (Map.Entry<Long, Double> longDoubleEntry : hitPointMapData.entrySet()) {
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
}
