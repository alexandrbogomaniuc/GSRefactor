package com.betsoft.casino.mp.bgsectorx.model.math.config;

import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.bgsectorx.model.math.EnemyData;
import com.betsoft.casino.mp.bgsectorx.model.math.EnemyType;
import com.betsoft.casino.mp.bgsectorx.model.math.KillerItemData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameConfig implements IGameConfig {
    private String mathVersion;
    private double gameRTP;
    private Map<EnemyType, List<EnemyData>> enemies;
    private Map<CriticalMultiplierType, List<Double>> criticalMultiplier;
    private Map<Integer, Double> BossPkillDiscount;
    //private Map<Integer, BossParams> bosses;
    private Map<Integer, BossParams> bosses2players;
    private Map<Integer, BossParams> bosses3players;
    private Map<Integer, BossParams> bosses4players;
    private Map<Integer, BossParams> bosses5players;
    private Map<Integer, BossParams> bosses6players;
    private Map<EnemyType, List<SpecialItem>> items;
    private Map<Integer, List<List<EnemyType>>> enemiesForItemsByPay;
    private Map<Integer, List<KillerItemData>> killItemsDataByPay;
    private Map<EnemyType, MinMaxParams> percentPayOnBoss;
    private TurretLevelUp TurretLevelUp;
    private Map<Integer, Double> TurretLevelRatio;
    private Map<Integer, List<Long>> BossRewardTiming;
    private int BossKillMultiplier;

    public GameConfig() {}

    public GameConfig(String mathVersion, double gameRTP, Map<EnemyType, List<EnemyData>> enemies, Map<CriticalMultiplierType,
            List<Double>> criticalMultiplier, Map<Integer, Double> bossPkillDiscount, Map<Integer, BossParams> bosses2players,
                      Map<Integer, BossParams> bosses3players, Map<Integer, BossParams> bosses4players,
                      Map<Integer, BossParams> bosses5players, Map<Integer, BossParams> bosses6players,
                      Map<EnemyType, List<SpecialItem>> items, Map<Integer, List<List<EnemyType>>> enemiesForItemsByPay,
                      Map<Integer, List<KillerItemData>> killItemsDataByPay, Map<EnemyType, MinMaxParams> percentPayOnBoss,
                      TurretLevelUp turretLevelUp, Map<Integer, Double> turretLevelRatio, Map<Integer, List<Long>> BossRewardTiming, int BossKillMultiplier) {
        this.mathVersion = mathVersion;
        this.gameRTP = gameRTP;
        this.enemies = enemies;
        this.criticalMultiplier = criticalMultiplier;
        this.BossPkillDiscount = bossPkillDiscount;
        this.bosses2players = bosses2players;
        this.bosses3players = bosses3players;
        this.bosses4players = bosses4players;
        this.bosses5players = bosses5players;
        this.bosses6players = bosses6players;
        this.items = items;
        this.enemiesForItemsByPay = enemiesForItemsByPay;
        this.killItemsDataByPay = killItemsDataByPay;
        this.percentPayOnBoss = percentPayOnBoss;
        this.TurretLevelUp = turretLevelUp;
        this.TurretLevelRatio = turretLevelRatio;
        this.BossRewardTiming = BossRewardTiming;
        this.BossKillMultiplier = BossKillMultiplier;
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

    public Map<Integer, Double> getBossPkillDiscount() {
        return BossPkillDiscount;
    }

    public void setBossPkillDiscount(Map<Integer, Double> bossPkillDiscount) {
        BossPkillDiscount = bossPkillDiscount;
    }

    public TurretLevelUp getTurretLevelUp() {
        return TurretLevelUp;
    }

    public void setTurretLevelUp(TurretLevelUp turretLevelUp) {
        TurretLevelUp = turretLevelUp;
    }

    public Map<Integer, Double> getTurretLevelRatio() {
        return TurretLevelRatio;
    }

    public void setTurretLevelRatio(Map<Integer, Double> turretLevelRatio) {
        TurretLevelRatio = turretLevelRatio;
    }

    public Map<Integer, List<KillerItemData>> getKillItemsDataByPay() {
        return killItemsDataByPay;
    }

    public void setKillItemsDataByPay(Map<Integer, List<KillerItemData>> killItemsDataByPay) {
        this.killItemsDataByPay = killItemsDataByPay;
    }

    public Map<Integer, BossParams> getBosses2players() {
        return bosses2players;
    }

    public void setBosses2players(Map<Integer, BossParams> bosses2players) {
        this.bosses2players = bosses2players;
    }

    public Map<Integer, BossParams> getBosses3players() {
        return bosses3players;
    }

    public void setBosses3players(Map<Integer, BossParams> bosses3players) {
        this.bosses3players = bosses3players;
    }

    public Map<Integer, BossParams> getBosses4players() {
        return bosses4players;
    }

    public void setBosses4players(Map<Integer, BossParams> bosses4players) {
        this.bosses4players = bosses4players;
    }

    public Map<Integer, BossParams> getBosses5players() {
        return bosses5players;
    }

    public void setBosses5players(Map<Integer, BossParams> bosses5players) {
        this.bosses5players = bosses5players;
    }

    public Map<Integer, BossParams> getBosses6players() {
        return bosses6players;
    }

    public void setBosses6players(Map<Integer, BossParams> bosses6players) {
        this.bosses6players = bosses6players;
    }

    public Map<Integer, List<Long>> getBossRewardTiming() {
        return BossRewardTiming;
    }

    public void setBossRewardTiming(Map<Integer, List<Long>> bossRewardTiming) {
        BossRewardTiming = bossRewardTiming;
    }

    public int getBossKillMultiplier() {
        return BossKillMultiplier;
    }

    public void setBossKillMultiplier(int bossKillMultiplier) {
        BossKillMultiplier = bossKillMultiplier;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameConfig{");
        sb.append("mathVersion='").append(mathVersion).append('\'');
        sb.append(", gameRTP=").append(gameRTP);
        sb.append(", enemies=").append(enemies);
        sb.append(", criticalMultiplier=").append(criticalMultiplier);
        sb.append(", bosses2players=").append(bosses2players);
        sb.append(", bosses3players=").append(bosses3players);
        sb.append(", bosses4players=").append(bosses4players);
        sb.append(", bosses5players=").append(bosses5players);
        sb.append(", bosses6players=").append(bosses6players);
        sb.append(", items=").append(items);
        sb.append(", enemiesForItemsByPay=").append(enemiesForItemsByPay);
        sb.append(", killItemsDataByPay=").append(killItemsDataByPay);
        sb.append(", percentPayOnBoss=").append(percentPayOnBoss);
        sb.append(", turretLevelRatio=").append(TurretLevelRatio);
        sb.append(", turretLevelUp=").append(TurretLevelUp);
        sb.append(", BossPkillDiscount=").append(BossPkillDiscount);
        sb.append(", BossRewardTiming=").append(BossRewardTiming);
        sb.append(", BossKillMultiplier=").append(BossKillMultiplier);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public Map<Integer, Integer> getWeaponPrices() {
        return Collections.emptyMap();
    }
}
