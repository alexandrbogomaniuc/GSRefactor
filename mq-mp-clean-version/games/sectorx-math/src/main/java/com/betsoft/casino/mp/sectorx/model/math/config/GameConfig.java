package com.betsoft.casino.mp.sectorx.model.math.config;

import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.sectorx.model.math.EnemyData;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.betsoft.casino.mp.sectorx.model.math.KillerItemData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameConfig implements IGameConfig {
    private String mathVersion;
    private double gameRTP;
    private Map<EnemyType, List<EnemyData>> enemies;
    private Map<CriticalMultiplierType, List<Double>> criticalMultiplier;
    private Map<Integer, BossParams> bosses;
    private Map<EnemyType, List<SpecialItem>> items;
    private Map<Integer, List<List<EnemyType>>> enemiesForItemsByPay;
    private Map<Integer, List<KillerItemData>> killItemsDataByPay;
    private Map<EnemyType, MinMaxParams> percentPayOnBoss;

    public GameConfig() {}

    public GameConfig(String mathVersion, double gameRTP, Map<EnemyType, List<EnemyData>> enemies,
                      Map<Integer, BossParams> bosses, Map<EnemyType, List<SpecialItem>> items,
                      Map<Integer, List<List<EnemyType>>> enemiesForItemsByPay, Map<Integer, List<KillerItemData>> killItemsDataByPay,
                      Map<EnemyType, MinMaxParams> percentPayOnBoss, Map<CriticalMultiplierType, List<Double>> criticalMultiplier) {
        this.mathVersion = mathVersion;
        this.gameRTP = gameRTP;
        this.enemies = enemies;
        this.bosses = bosses;
        this.items = items;
        this.enemiesForItemsByPay = Collections.unmodifiableMap(enemiesForItemsByPay);
        this.killItemsDataByPay = Collections.unmodifiableMap(killItemsDataByPay);
        this.percentPayOnBoss = percentPayOnBoss;
        this.criticalMultiplier = criticalMultiplier;
    }

    public Map<CriticalMultiplierType, List<Double>> getCriticalMultiplier() {
        return criticalMultiplier;
    }

    public void setCriticalMultiplier(Map<CriticalMultiplierType, List<Double>> criticalMultiplier) {
        this.criticalMultiplier = criticalMultiplier;
    }

    public boolean enemyTypeEnabled(EnemyType enemyType){
        return getEnemies().get(enemyType).get(0).isEnabled();
    }

    public Map<EnemyType, MinMaxParams> getPercentPayOnBoss() {
        return percentPayOnBoss;
    }

    public void setPercentPayOnBoss(Map<EnemyType, MinMaxParams> percentPayOnBoss) {
        this.percentPayOnBoss = percentPayOnBoss;
    }

    public double getGameRTP() {
        return gameRTP;
    }

    public Map<EnemyType, List<EnemyData>> getEnemies() {
        return enemies;
    }

    public EnemyData getEnemyData(EnemyType enemyType) {
        return enemies.get(enemyType).get(0);
    }

    public Map<Integer, BossParams> getBosses() {
        return bosses;
    }

    public String getMathVersion() {
        return mathVersion;
    }

    public void setMathVersion(String mathVersion) {
        this.mathVersion = mathVersion;
    }

    public void setGameRTP(double gameRTP) {
        this.gameRTP = gameRTP;
    }

    public void setEnemies(Map<EnemyType, List<EnemyData>> enemies) {
        this.enemies = enemies;
    }

    public void setBosses(Map<Integer, BossParams> bosses) {
        this.bosses = bosses;
    }

    public Map<EnemyType, List<SpecialItem>> getItems() {
        return items;
    }

    public void setItems(Map<EnemyType, List<SpecialItem>> items) {
        this.items = items;
    }

    public Map<Integer, List<List<EnemyType>>> getEnemiesForItemsByPay() {
        return enemiesForItemsByPay;
    }

    public void setEnemiesForItemsByPay(Map<Integer, List<List<EnemyType>>> enemiesForItemsByPay) {
        this.enemiesForItemsByPay = enemiesForItemsByPay;
    }

    public Map<Integer, List<KillerItemData>> getKillItemsDataByPay() {
        return killItemsDataByPay;
    }

    public void setKillItemsDataByPay(Map<Integer, List<KillerItemData>> killItemsDataByPay) {
        this.killItemsDataByPay = killItemsDataByPay;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameConfig{");
        sb.append("mathVersion='").append(mathVersion).append('\'');
        sb.append(", gameRTP=").append(gameRTP);
        sb.append(", enemies=").append(enemies);
        sb.append(", criticalMultiplier=").append(criticalMultiplier);
        sb.append(", bosses=").append(bosses);
        sb.append(", items=").append(items);
        sb.append(", enemiesForItemsByPay=").append(enemiesForItemsByPay);
        sb.append(", killItemsDataByPay=").append(killItemsDataByPay);
        sb.append(", percentPayOnBoss=").append(percentPayOnBoss);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public Map<Integer, Integer> getWeaponPrices() {
        return Collections.emptyMap();
    }
}
