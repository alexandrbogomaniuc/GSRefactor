package com.betsoft.casino.mp.bgmissionamazon.model.math.config;

import com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyData;
import com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyType;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GameConfig implements IGameConfig {
    private String mathVersion;
    private List<WeaponDrop> weaponDropsATargetEn;
    private List<WeaponDrop> weaponDropsATargetBoss;
    private List<WeaponDrop> weaponDropsBTargetEn;
    private List<WeaponDrop> weaponDropsBTargetBoss;

    private Map<Integer, Double> turretTargetsEn;
    private Map<SpecialWeaponType, Map<Integer, Double>> weaponTargetsEn;
    private Map<Integer, Double> turretTargetsBoss;
    private Map<SpecialWeaponType, Map<Integer, Double>> weaponTargetsBoss;

    private Map<Integer, Double> criticalHitTurretMultipliersTargetEn;
    private Map<SpecialWeaponType, Map<Integer, Double>> criticalHitMultipliersTargetEn;

    private Map<Integer, Double> criticalHitTurretMultipliersTargetBoss;
    private Map<SpecialWeaponType, Map<Integer, Double>> criticalHitMultipliersTargetBoss;
    private Map<EnemyType, List<EnemyData>> enemies;
    private BossParams bossParams;
    private QuestParams questParams;
    private Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetEn;
    private Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetBoss;
    private Map<SpecialWeaponType, Double> PSWDropsReTriggerOneTargetEn;
    private Map<SpecialWeaponType, Double> PSWDropsReTriggerTwoTargetEn;
    private Map<SpecialWeaponType, Double> PSWDropsReTriggerOneTargetBoss;
    private Map<SpecialWeaponType, Double> PSWDropsReTriggerTwoTargetBoss;
    private Map<Integer, Map<Integer, Double>> powerUpMultipliers;

    public GameConfig() {}

    public GameConfig(String mathVersion, List<WeaponDrop> weaponDropsATargetEn,
                      List<WeaponDrop> weaponDropsATargetBoss, List<WeaponDrop> weaponDropsBTargetEn,
                      List<WeaponDrop> weaponDropsBTargetBoss,
                      Map<Integer, Double> turretTargetsEn,
                      Map<SpecialWeaponType, Map<Integer, Double>> weaponTargetsEn,
                      Map<Integer, Double> turretTargetsBoss,
                      Map<SpecialWeaponType, Map<Integer, Double>> weaponTargetsBoss,
                      Map<Integer, Double> criticalHitTurretMultipliersTargetEn,
                      Map<SpecialWeaponType, Map<Integer, Double>> criticalHitMultipliersTargetEn,
                      Map<Integer, Double> criticalHitTurretMultipliersTargetBoss,
                      Map<SpecialWeaponType, Map<Integer, Double>> criticalHitMultipliersTargetBoss,
                      Map<EnemyType, List<EnemyData>> enemies,
                      BossParams bossParams, QuestParams questParams,
                      Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetEn,
                      Map<EnemyType, List<WeaponDrop>> weaponCarrierDropsTargetBoss,
                      Map<SpecialWeaponType, Double> PSWDropsReTriggerOneTargetEn,
                      Map<SpecialWeaponType, Double> PSWDropsReTriggerTwoTargetEn,
                      Map<SpecialWeaponType, Double> PSWDropsReTriggerOneTargetBoss,
                      Map<SpecialWeaponType, Double> PSWDropsReTriggerTwoTargetBoss,
                      Map<Integer, Map<Integer, Double>> powerUpMultipliers) {
        this.mathVersion = mathVersion;
        this.weaponDropsATargetEn = weaponDropsATargetEn;
        this.weaponDropsATargetBoss = weaponDropsATargetBoss;
        this.weaponDropsBTargetEn = weaponDropsBTargetEn;
        this.weaponDropsBTargetBoss = weaponDropsBTargetBoss;
        this.turretTargetsEn = turretTargetsEn;
        this.weaponTargetsEn = weaponTargetsEn;
        this.turretTargetsBoss = turretTargetsBoss;
        this.weaponTargetsBoss = weaponTargetsBoss;
        this.criticalHitTurretMultipliersTargetEn = criticalHitTurretMultipliersTargetEn;
        this.criticalHitMultipliersTargetEn = criticalHitMultipliersTargetEn;
        this.criticalHitTurretMultipliersTargetBoss = criticalHitTurretMultipliersTargetBoss;
        this.criticalHitMultipliersTargetBoss = criticalHitMultipliersTargetBoss;
        this.enemies = enemies;
        this.bossParams = bossParams;
        this.questParams = questParams;
        this.weaponCarrierDropsTargetEn = weaponCarrierDropsTargetEn;
        this.weaponCarrierDropsTargetBoss = weaponCarrierDropsTargetBoss;
        this.PSWDropsReTriggerOneTargetEn = PSWDropsReTriggerOneTargetEn;
        this.PSWDropsReTriggerTwoTargetEn = PSWDropsReTriggerTwoTargetEn;
        this.PSWDropsReTriggerOneTargetBoss = PSWDropsReTriggerOneTargetBoss;
        this.PSWDropsReTriggerTwoTargetBoss = PSWDropsReTriggerTwoTargetBoss;
        this.powerUpMultipliers = powerUpMultipliers;
    }

    public boolean isEnabledEnemy(EnemyType enemyType) {
        List<EnemyData> enemyData = getEnemies().get(enemyType);
        return enemyData != null && !enemyData.isEmpty() && enemyData.get(0).isEnabled();
    }

    public Map<SpecialWeaponType, Double> getPSWDropsReTriggerOneTargetEn() {
        return PSWDropsReTriggerOneTargetEn;
    }

    public Map<SpecialWeaponType, Double> getPSWDropsReTriggerTwoTargetEn() {
        return PSWDropsReTriggerTwoTargetEn;
    }

    public Map<SpecialWeaponType, Double> getPSWDropsReTriggerOneTargetBoss() {
        return PSWDropsReTriggerOneTargetBoss;
    }

    public Map<SpecialWeaponType, Double> getPSWDropsReTriggerTwoTargetBoss() {
        return PSWDropsReTriggerTwoTargetBoss;
    }

    public List<WeaponDrop> getWeaponDropsATargetEn() {
        return weaponDropsATargetEn;
    }

    public List<WeaponDrop> getWeaponDropsBTargetEn() {
        return weaponDropsBTargetEn;
    }

    public List<WeaponDrop> getWeaponDropsATargetBoss() {
        return weaponDropsATargetBoss;
    }

    public List<WeaponDrop> getWeaponDropsBTargetBoss() {
        return weaponDropsBTargetBoss;
    }

    @Override
    public Map<Integer, Integer> getWeaponPrices() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Integer, Double> getGemPrizes(Money stake, int betLevel) {
        return questParams.getGemDrops().stream()
                .collect(Collectors.toMap(GemDrop::getType, (e -> stake.multiply((long) e.getPrize() * betLevel).toDoubleCents())));
    }

    public Map<SpecialWeaponType, Map<Integer, Double>> getWeaponTargetsEn() {
        return weaponTargetsEn;
    }

    public Map<Integer, Double> getWeaponTargets(SpecialWeaponType weaponType) {
        return weaponTargetsEn.get(weaponType);
    }

    public Map<Integer, Double> getTurretTargetsEn() {
        return turretTargetsEn;
    }

    public Map<Integer, Double> getTurretTargetsBoss() {
        return turretTargetsBoss;
    }

    public Map<SpecialWeaponType, Map<Integer, Double>> getWeaponTargetsBoss() {
        return weaponTargetsBoss;
    }

    public Map<SpecialWeaponType, Map<Integer, Double>> getCriticalHitMultipliersTargetEn() {
        return criticalHitMultipliersTargetEn;
    }

    public Map<SpecialWeaponType, Map<Integer, Double>> getCriticalHitMultipliersTargetBoss() {
        return criticalHitMultipliersTargetBoss;
    }

    public Map<Integer, Double> getCriticalHitTurretMultipliersTargetEn() {
        return criticalHitTurretMultipliersTargetEn;
    }

    public Map<Integer, Double> getCriticalHitTurretMultipliersTargetBoss() {
        return criticalHitTurretMultipliersTargetBoss;
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

    public String getMathVersion() {
        return mathVersion;
    }

    public Map<Integer, Map<Integer, Double>> getPowerUpMultipliers() {
        return powerUpMultipliers;
    }

    @Override
    public String toString() {
        return "GameConfig{" +
                "mathVersion='" + mathVersion + '\'' +
                ", weaponDropsATargetEn=" + weaponDropsATargetEn +
                ", weaponDropsATargetBoss=" + weaponDropsATargetBoss +
                ", weaponDropsBTargetEn=" + weaponDropsBTargetEn +
                ", turretTargetsEn=" + turretTargetsEn +
                ", weaponTargetsEn=" + weaponTargetsEn +
                ", turretTargetsBoss=" + turretTargetsBoss +
                ", weaponTargetsBoss=" + weaponTargetsBoss +
                ", criticalHitTurretMultipliersTargetEn=" + criticalHitTurretMultipliersTargetEn +
                ", criticalHitMultipliersTargetEn=" + criticalHitMultipliersTargetEn +
                ", criticalHitTurretMultipliersTargetBoss=" + criticalHitTurretMultipliersTargetBoss +
                ", criticalHitMultipliersTargetBoss=" + criticalHitMultipliersTargetBoss +
                ", enemies=" + enemies +
                ", bossParams=" + bossParams +
                ", questParams=" + questParams +
                ", weaponCarrierDropsTargetEn=" + weaponCarrierDropsTargetEn +
                ", weaponCarrierDropsTargetBoss=" + weaponCarrierDropsTargetBoss +
                ", PSWDropsReTriggerOneTargetEn=" + PSWDropsReTriggerOneTargetEn +
                ", PSWDropsReTriggerTwoTargetEn=" + PSWDropsReTriggerTwoTargetEn +
                ", PSWDropsReTriggerOneTargetBoss=" + PSWDropsReTriggerOneTargetBoss +
                ", PSWDropsReTriggerTwoTargetBoss=" + PSWDropsReTriggerTwoTargetBoss +
                ", powerUpMultipliers=" + powerUpMultipliers +
                '}';
    }
}
