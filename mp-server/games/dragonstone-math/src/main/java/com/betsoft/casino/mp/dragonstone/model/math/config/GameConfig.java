package com.betsoft.casino.mp.dragonstone.model.math.config;

import com.betsoft.casino.mp.dragonstone.model.math.EnemyData;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;

import java.util.List;
import java.util.Map;

public class GameConfig implements IGameConfig {
    private double gameRTP;
    private Map<Integer, Double> slotContribution;
    private Map<Integer, Double> weaponDropSpecialWeaponRTP;
    private Map<Integer, Integer> weaponPrices;
    private Map<Integer, Map<Integer, Double>> weaponTargets;
    private List<WeaponDrop> weaponDrops;
    private Map<Integer, Map<Integer, Double>> criticalHitMultipliers;
    private Map<EnemyType, List<EnemyData>> enemies;
    private int rageMin;
    private int rageMax;
    private int spiritMin;
    private int spiritMax;
    private BossParams boss;
    private SlotParams slot;
    private FragmentParams fragments;
    private boolean ignoreRules;

    public GameConfig() {}

    public GameConfig(double gameRTP, Map<Integer, Double> slotContribution, Map<Integer, Double> weaponDropSpecialWeaponRTP,
                      Map<Integer, Integer> weaponPrices,
                      Map<Integer, Map<Integer, Double>> weaponTargets, List<WeaponDrop> weaponDrops,
                      Map<Integer, Map<Integer, Double>> criticalHitMultipliers,
                      Map<EnemyType, List<EnemyData>> enemies,
                      BossParams boss, SlotParams slot, FragmentParams fragments, boolean ignoreRules,
                      int rageMin, int rageMax, int spiritMin, int spiritMax) {
        this.gameRTP = gameRTP;
        this.slotContribution = slotContribution;
        this.weaponDropSpecialWeaponRTP = weaponDropSpecialWeaponRTP;
        this.weaponPrices = weaponPrices;
        this.weaponTargets = weaponTargets;
        this.weaponDrops = weaponDrops;
        this.criticalHitMultipliers = criticalHitMultipliers;
        this.enemies = enemies;
        this.boss = boss;
        this.slot = slot;
        this.fragments = fragments;
        this.ignoreRules = ignoreRules;
        this.rageMin = rageMin;
        this.rageMax = rageMax;
        this.spiritMin = spiritMin;
        this.spiritMax = spiritMax;
    }

    public double getGameRTP() {
        return gameRTP;
    }

    public Map<Integer, Double> getSlotContribution() {
        return slotContribution;
    }

    public Map<Integer, Double> getWeaponDropSpecialWeaponRTP() {
        return weaponDropSpecialWeaponRTP;
    }

    @Override
    public Map<Integer, Integer> getWeaponPrices() {
        return weaponPrices;
    }

    public Integer getWeaponPrice(int weaponId) {
        return weaponPrices.get(weaponId);
    }

    public Map<Integer, Map<Integer, Double>> getWeaponTargets() {
        return weaponTargets;
    }

    public Map<Integer, Double> getWeaponTargets(int weaponId) {
        return weaponTargets.get(weaponId);
    }

    public List<WeaponDrop> getWeaponDrops() {
        return weaponDrops;
    }

    public Map<Integer, Map<Integer, Double>> getCriticalHitMultipliers() {
        return criticalHitMultipliers;
    }

    public Map<Integer, Double> getCriticalHitMultipliers(int weaponId) {
        return criticalHitMultipliers.get(weaponId);
    }

    public Map<EnemyType, List<EnemyData>> getEnemies() {
        return enemies;
    }

    public EnemyData getEnemyData(EnemyType enemyType, int idxData) {
        return enemies.get(enemyType).get(idxData);
    }

    public BossParams getBoss() {
        return boss;
    }

    public SlotParams getSlot() {
        return slot;
    }

    public FragmentParams getFragments() {
        return fragments;
    }

    public boolean isEnemyEnabled(EnemyType enemyType) {
        return enemies.get(enemyType).get(0).isEnabled();
    }

    public boolean isIgnoreRules() {
        return ignoreRules;
    }

    public int getRageMin() {
        return rageMin;
    }

    public void setRageMin(int rageMin) {
        this.rageMin = rageMin;
    }

    public int getRageMax() {
        return rageMax;
    }

    public void setRageMax(int rageMax) {
        this.rageMax = rageMax;
    }

    public int getSpiritMin() {
        return spiritMin;
    }

    public void setSpiritMin(int spiritMin) {
        this.spiritMin = spiritMin;
    }

    public int getSpiritMax() {
        return spiritMax;
    }

    public void setSpiritMax(int spiritMax) {
        this.spiritMax = spiritMax;
    }

    @Override
    public String toString() {
        return "GameConfig{" +
                "gameRTP=" + gameRTP +
                ", slotContribution=" + slotContribution +
                ", weaponDropSpecialWeaponRTP=" + weaponDropSpecialWeaponRTP +
                ", weaponPrices=" + weaponPrices +
                ", weaponTargets=" + weaponTargets +
                ", weaponDrops=" + weaponDrops +
                ", criticalHitMultipliers=" + criticalHitMultipliers +
                ", enemies=" + enemies +
                ", boss=" + boss +
                ", slot=" + slot +
                ", fragments=" + fragments +
                ", ignoreRules=" + ignoreRules +
                ", rageMin=" + rageMin +
                ", rageMax=" + rageMax +
                ", spiritMin=" + spiritMin +
                ", spiritMax=" + spiritMax +
                '}';
    }
}
