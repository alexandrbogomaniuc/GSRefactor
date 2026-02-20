package com.betsoft.casino.mp.bgdragonstone.model.math.config;

import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyData;
import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameConfig implements IGameConfig {
    private int mathVersion;
    private Map<Integer, Map<Integer, Double>> weaponTargets;
    private List<WeaponDrop> weaponDropsA;
    private List<WeaponDrop> weaponDropsB;
    private List<WeaponDrop> weaponDropsC;
    private Map<Integer, Map<Integer, Double>> criticalHitMultipliers;
    private Map<EnemyType, List<EnemyData>> enemies;
    private int rageMin;
    private int rageMax;
    private int spiritMin;
    private int spiritMax;
    private BossParams boss;
    private SlotParams slot;
    private FragmentParams fragments;
    private Map<Integer, Double> PSWDropsReTriggerOne;
    private Map<Integer, Double> PSWDropsReTriggerTwo;

    public GameConfig() {}

    public GameConfig(int mathVersion, Map<Integer, Map<Integer, Double>> weaponTargets, List<WeaponDrop> weaponDropsA,
                      List<WeaponDrop> weaponDropsB, List<WeaponDrop> weaponDropsC,
                      Map<Integer, Map<Integer, Double>> criticalHitMultipliers, Map<EnemyType, List<EnemyData>> enemies,
                      int rageMin, int rageMax, int spiritMin, int spiritMax, BossParams boss, SlotParams slot,
                      FragmentParams fragments, Map<Integer, Double> PSWDropsReTriggerOne,
                      Map<Integer, Double> PSWDropsReTriggerTwo) {
        this.mathVersion = mathVersion;
        this.weaponTargets = weaponTargets;
        this.weaponDropsA = weaponDropsA;
        this.weaponDropsB = weaponDropsB;
        this.weaponDropsC = weaponDropsC;
        this.criticalHitMultipliers = criticalHitMultipliers;
        this.enemies = enemies;
        this.rageMin = rageMin;
        this.rageMax = rageMax;
        this.spiritMin = spiritMin;
        this.spiritMax = spiritMax;
        this.boss = boss;
        this.slot = slot;
        this.fragments = fragments;
        this.PSWDropsReTriggerOne = PSWDropsReTriggerOne;
        this.PSWDropsReTriggerTwo = PSWDropsReTriggerTwo;
    }

    public List<WeaponDrop> getWeaponDropsA() {
        return weaponDropsA;
    }

    public List<WeaponDrop> getWeaponDropsB() {
        return weaponDropsB;
    }

    public List<WeaponDrop> getWeaponDropsC() {
        return weaponDropsC;
    }

    public List<WeaponDrop> getWeaponDrops(int weaponTypeId) {
        switch (weaponTypeId) {
            case -1:
                return weaponDropsA;
            case 16:
                return weaponDropsC;
            default:
                return weaponDropsB;
        }
    }


    public Map<Integer, Double> getPSWDropsReTriggerOne() {
        return PSWDropsReTriggerOne;
    }

    public void setPSWDropsReTriggerOne(Map<Integer, Double> PSWDropsReTriggerOne) {
        this.PSWDropsReTriggerOne = PSWDropsReTriggerOne;
    }

    public Map<Integer, Double> getPSWDropsReTriggerTwo() {
        return PSWDropsReTriggerTwo;
    }

    public void setPSWDropsReTriggerTwo(Map<Integer, Double> PSWDropsReTriggerTwo) {
        this.PSWDropsReTriggerTwo = PSWDropsReTriggerTwo;
    }

    @Override
    public Map<Integer, Integer> getWeaponPrices() {
        return Collections.emptyMap();
    }


    public Map<Integer, Map<Integer, Double>> getWeaponTargets() {
        return weaponTargets;
    }

    public Map<Integer, Double> getWeaponTargets(int weaponId) {
        return weaponTargets.get(weaponId);
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

    public int getMathVersion() {
        return mathVersion;
    }

    public void setMathVersion(int mathVersion) {
        this.mathVersion = mathVersion;
    }

    public void setWeaponTargets(Map<Integer, Map<Integer, Double>> weaponTargets) {
        this.weaponTargets = weaponTargets;
    }

    public void setWeaponDropsA(List<WeaponDrop> weaponDropsA) {
        this.weaponDropsA = weaponDropsA;
    }

    public void setWeaponDropsB(List<WeaponDrop> weaponDropsB) {
        this.weaponDropsB = weaponDropsB;
    }

    public void setWeaponDropsC(List<WeaponDrop> weaponDropsC) {
        this.weaponDropsC = weaponDropsC;
    }

    public void setCriticalHitMultipliers(Map<Integer, Map<Integer, Double>> criticalHitMultipliers) {
        this.criticalHitMultipliers = criticalHitMultipliers;
    }

    public void setEnemies(Map<EnemyType, List<EnemyData>> enemies) {
        this.enemies = enemies;
    }

    public void setBoss(BossParams boss) {
        this.boss = boss;
    }

    public void setSlot(SlotParams slot) {
        this.slot = slot;
    }

    public void setFragments(FragmentParams fragments) {
        this.fragments = fragments;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameConfig{");
        sb.append("mathVersion=").append(mathVersion);
        sb.append(", weaponTargets=").append(weaponTargets);
        sb.append(", weaponDropsA=").append(weaponDropsA);
        sb.append(", weaponDropsB=").append(weaponDropsB);
        sb.append(", weaponDropsC=").append(weaponDropsC);
        sb.append(", criticalHitMultipliers=").append(criticalHitMultipliers);
        sb.append(", enemies=").append(enemies);
        sb.append(", rageMin=").append(rageMin);
        sb.append(", rageMax=").append(rageMax);
        sb.append(", spiritMin=").append(spiritMin);
        sb.append(", spiritMax=").append(spiritMax);
        sb.append(", boss=").append(boss);
        sb.append(", slot=").append(slot);
        sb.append(", fragments=").append(fragments);
        sb.append(", PSWDropsReTriggerOne=").append(PSWDropsReTriggerOne);
        sb.append(", PSWDropsReTriggerTwo=").append(PSWDropsReTriggerTwo);
        sb.append('}');
        return sb.toString();
    }
}
