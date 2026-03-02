package com.betsoft.casino.mp.clashofthegods.model.math.enemies;

import com.betsoft.casino.mp.clashofthegods.model.math.WeaponData;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.Triple;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IEnemyData {
    Map<Integer, WeaponData[]> getFreeWeaponData();
    Map<Integer, WeaponData[]> getPaidWeaponData();
    Map<Integer, Pair<Double, Double>> getWeaponDropData();
    HashMap<Integer, List<Triple<Integer, Integer, Double>>> getAdditionalWeaponKilledTable();
    Double getSwAvgPayouts(int weaponId, int levelId, double payMultiplier, boolean paidMode,
                           Map<Long, Double> hitPointMapData);
    WeaponData getWeaponDataMap(int weaponId, int levelId, boolean paidMode);
    int[] getLevels();
}

