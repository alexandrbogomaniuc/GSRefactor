package com.betsoft.casino.mp.piratescommon.model.math;

import com.betsoft.casino.mp.model.gameconfig.GameTools;

import java.util.HashMap;
import java.util.Map;

public class WeaponData {
    private final Map<Integer, Double> criticalHitProb; // xMult, prob
    private final double instantKillEV;
    private final double limitForHitPointMap;
    private final Map<Long, Double> enemyHiHitPointMap;
    private final Map<Long, Double> enemyMiddleHitPointMap;
    private final Map<Long, Double> enemyLowHitPointMap;
    private final double limitForMiddleHitPointMap;

    public WeaponData(Map<Integer, Double> criticalHitProb, double instantKillEV,
                      Map<Long, Double> enemyHiHitPointMap) {
        this.criticalHitProb = criticalHitProb;
        this.instantKillEV = instantKillEV;
        this.limitForHitPointMap = 0;
        this.enemyHiHitPointMap = enemyHiHitPointMap;
        this.enemyLowHitPointMap = new HashMap<>();
        this.limitForMiddleHitPointMap = 0;
        this.enemyMiddleHitPointMap = new HashMap<>();
    }

    public WeaponData(Map<Integer, Double> criticalHitProb, double instantKillEV, double limitForHitPointMap,
                      Map<Long, Double> enemyHiHitPointMap, Map<Long, Double> enemyLowHitPointMap) {
        this.criticalHitProb = criticalHitProb;
        this.instantKillEV = instantKillEV;
        this.limitForHitPointMap = limitForHitPointMap;
        this.enemyHiHitPointMap = enemyHiHitPointMap;
        this.enemyLowHitPointMap = enemyLowHitPointMap;
        this.limitForMiddleHitPointMap = 0;
        this.enemyMiddleHitPointMap = new HashMap<>();
    }

    public WeaponData(Map<Integer, Double> criticalHitProb, double instantKillEV, double limitForHitPointMap,
                      double limitForMiddleHitPointMap,
                      Map<Long, Double> enemyHiHitPointMap,
                      Map<Long, Double> enemyMiddleHitPointMap,
                      Map<Long, Double> enemyLowHitPointMap) {
        this.criticalHitProb = criticalHitProb;
        this.instantKillEV = instantKillEV;
        this.limitForHitPointMap = limitForHitPointMap;
        this.enemyHiHitPointMap = enemyHiHitPointMap;
        this.enemyLowHitPointMap = enemyLowHitPointMap;
        this.limitForMiddleHitPointMap = limitForMiddleHitPointMap;
        this.enemyMiddleHitPointMap = enemyMiddleHitPointMap;
    }

    public Map<Long, Double> getEnemyMiddleHitPointMap() {
        return enemyMiddleHitPointMap;
    }

    public double getLimitForMiddleHitPointMap() {
        return limitForMiddleHitPointMap;
    }


    public Map<Integer, Double> getCriticalHitProb() {
        return criticalHitProb;
    }

    public Integer getRandomCriticalHit() {
        double summ = criticalHitProb.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<Integer, Double> hitProbFull = new HashMap<>(criticalHitProb);
        hitProbFull.put(0, 100. - summ);
        Integer mult = GameTools.getRandomNumberKeyFromMapWithNorm(hitProbFull);
        return mult == 0 ? 1 : mult;
    }

    public double getInstantKillEV() {
        return instantKillEV;
    }

    public double getLimitForHitPointMap() {
        return limitForHitPointMap;
    }

    public Map<Long, Double> getEnemyHiHitPointMap() {
        return enemyHiHitPointMap;
    }

    public Map<Long, Double> getEnemyLowHitPointMap() {
        return enemyLowHitPointMap;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeaponData{");
        sb.append("criticalHitProb=").append(criticalHitProb);
        sb.append(", instantKillEV=").append(instantKillEV);
        sb.append(", limitForHitPointMap=").append(limitForHitPointMap);
        sb.append(", limitForMiddleHitPointMap=").append(limitForMiddleHitPointMap);
        sb.append(", enemyHiHitPointMap=").append(enemyHiHitPointMap);
        sb.append(", enemyLowHitPointMap=").append(enemyLowHitPointMap);
        sb.append(", enemyMiddleHitPointMap=").append(enemyMiddleHitPointMap);
        sb.append('}');
        return sb.toString();
    }
}
