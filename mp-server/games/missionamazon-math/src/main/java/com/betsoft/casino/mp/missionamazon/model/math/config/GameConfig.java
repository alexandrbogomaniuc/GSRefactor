package com.betsoft.casino.mp.missionamazon.model.math.config;

import com.betsoft.casino.mp.missionamazon.model.math.EnemyData;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyType;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;

import java.util.List;
import java.util.Map;

public class GameConfig implements IGameConfig {
    private String mathVersion;
    private double gameRTP;
    private Map<Integer, Double> weaponDropSpecialWeaponRTPTargetEn;
    private Map<Integer, Double> weaponDropSpecialWeaponRTPTargetBoss;
    private Map<Integer, Integer> weaponPrices;
    private Map<Integer, Map<Integer, Double>> weaponTargetsEn;
    private Map<Integer, Map<Integer, Double>> weaponTargetsBoss;
    private List<WeaponDrop> weaponDropsTargetEn;
    private List<WeaponDrop> weaponDropsTargetBoss;
    private Map<Integer, Map<Integer, Double>> criticalHitMultipliersTargetEn;
    private Map<Integer, Map<Integer, Double>> criticalHitMultipliersTargetBoss;
    private Map<EnemyType, List<EnemyData>> enemies;
    private BossParams bossParams;
    private QuestParams questParams;
    private Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetEn;
    private Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetBoss;
    private boolean ignoreRules;

    public GameConfig() {}

    public GameConfig(String mathVersion, double gameRTP,
                      Map<Integer, Double> weaponDropSpecialWeaponRTPTargetEn, Map<Integer, Double> weaponDropSpecialWeaponRTPTargetBoss,
                      Map<Integer, Integer> weaponPrices,
                      Map<Integer, Map<Integer, Double>> weaponTargetsEn, Map<Integer, Map<Integer, Double>> weaponTargetsBoss,
                      List<WeaponDrop> weaponDropsTargetEn, List<WeaponDrop> weaponDropsTargetBoss,
                      Map<Integer, Map<Integer, Double>> criticalHitMultipliersTargetEn, Map<Integer, Map<Integer, Double>> criticalHitMultipliersTargetBoss,
                      Map<EnemyType, List<EnemyData>> enemies,
                      boolean ignoreRules, BossParams bossParams, QuestParams questParams,
                      Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetEn, Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetBoss) {
        this.mathVersion = mathVersion;
        this.gameRTP = gameRTP;
        this.weaponDropSpecialWeaponRTPTargetEn = weaponDropSpecialWeaponRTPTargetEn;
        this.weaponDropSpecialWeaponRTPTargetBoss = weaponDropSpecialWeaponRTPTargetBoss;
        this.weaponPrices = weaponPrices;
        this.weaponTargetsEn = weaponTargetsEn;
        this.weaponTargetsBoss = weaponTargetsBoss;
        this.weaponDropsTargetEn = weaponDropsTargetEn;
        this.weaponDropsTargetBoss = weaponDropsTargetBoss;
        this.criticalHitMultipliersTargetEn = criticalHitMultipliersTargetEn;
        this.criticalHitMultipliersTargetBoss = criticalHitMultipliersTargetBoss;
        this.enemies = enemies;
        this.ignoreRules = ignoreRules;
        this.bossParams = bossParams;
        this.questParams = questParams;
        this.weaponCarrierDropsTargetEn = weaponCarrierDropsTargetEn;
        this.weaponCarrierDropsTargetBoss = weaponCarrierDropsTargetBoss;
    }

    public List<WeaponDrop> getWeaponDropsTargetEn() {
        return weaponDropsTargetEn;
    }

    public List<WeaponDrop> getWeaponDropsTargetBoss() {
        return weaponDropsTargetBoss;
    }

    public double getGameRTP() {
        return gameRTP;
    }


    public Map<Integer, Double> getWeaponDropSpecialWeaponRTPTargetEn() {
        return weaponDropSpecialWeaponRTPTargetEn;
    }

    public Map<Integer, Double> getWeaponDropSpecialWeaponRTPTargetBoss() {
        return weaponDropSpecialWeaponRTPTargetBoss;
    }

    @Override
    public Map<Integer, Integer> getWeaponPrices() {
        return weaponPrices;
    }

    public Integer getWeaponPrice(int weaponId) {
        return weaponPrices.get(weaponId);
    }

    public Map<Integer, Map<Integer, Double>> getWeaponTargetsEn() {
        return weaponTargetsEn;
    }

    public Map<Integer, Double> getWeaponTargetsEn(int weaponId) {
        return weaponTargetsEn.get(weaponId);
    }

    public Map<Integer, Map<Integer, Double>> getWeaponTargetsBoss() {
        return weaponTargetsBoss;
    }

    public Map<Integer, Double> getWeaponTargetsBoss(int weaponId) {
        return weaponTargetsBoss.get(weaponId);
    }

    public Map<Integer, Map<Integer, Double>> getCriticalHitMultipliersTargetEn() {
        return criticalHitMultipliersTargetEn;
    }

    public Map<Integer, Double> getCriticalHitMultipliersTargetEn(int weaponId) {
        return criticalHitMultipliersTargetEn.get(weaponId);
    }

    public Map<Integer, Map<Integer, Double>> getCriticalHitMultipliersTargetBoss() {
        return criticalHitMultipliersTargetBoss;
    }

    public Map<Integer, Double> getCriticalHitMultipliersTargetBoss(int weaponId) {
        return criticalHitMultipliersTargetBoss.get(weaponId);
    }

    public Map<EnemyType, List<EnemyData>> getEnemies() {
        return enemies;
    }

    public EnemyData getEnemyData(EnemyType enemyType, int idxData) {
        return enemies.get(enemyType).get(idxData);
    }

    public boolean isEnemyEnabled(EnemyType enemyType) {
        return enemies.get(enemyType).get(0).isEnabled();
    }

    public BossParams getBossParams() {
        return bossParams;
    }

    public QuestParams getQuestParams() {
        return questParams;
    }

    public Map<EnemyType, List<WeaponDrop>> getWeaponCarrierDropsTargetEn() {
        return weaponCarrierDropsTargetEn;
    }

    public Map<EnemyType, List<WeaponDrop>> getWeaponCarrierDropsTargetBoss() {
        return weaponCarrierDropsTargetBoss;
    }

    public boolean isIgnoreRules() {
        return ignoreRules;
    }

    public String getMathVersion() {
        return mathVersion;
    }

    public void setMathVersion(String mathVersion) {
        this.mathVersion = mathVersion;
    }

    @Override
    public String toString() {
        return "GameConfig{" +
                "mathVersion=" + mathVersion +
                ", gameRTP=" + gameRTP +
                ", weaponDropSpecialWeaponRTPTargetEn=" + weaponDropSpecialWeaponRTPTargetEn +
                ", weaponDropSpecialWeaponRTPTargetBoss=" + weaponDropSpecialWeaponRTPTargetBoss +
                ", weaponPrices=" + weaponPrices +
                ", weaponTargets=" + weaponTargetsEn +
                ", weaponDropsTargetEn=" + weaponDropsTargetEn +
                ", weaponDropsTargetBoss=" + weaponDropsTargetBoss +
                ", criticalHitMultipliersTargetEn=" + criticalHitMultipliersTargetEn +
                ", criticalHitMultipliersTargetBoss=" + criticalHitMultipliersTargetBoss +
                ", enemies=" + enemies +
                ", bossParams=" + bossParams +
                ", questParams=" + questParams +
                ", weaponCarrierDropsTargetEn=" + weaponCarrierDropsTargetEn +
                ", weaponCarrierDropsTargetBoss=" + weaponCarrierDropsTargetBoss +
                ", ignoreRules=" + ignoreRules +
                '}';
    }
}
