package com.betsoft.casino.mp.common.math;

import com.betsoft.casino.mp.model.IPaytable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.*;

public class Paytable implements KryoSerializable, Serializable, IPaytable {
    private static final byte VERSION = 0;
    private List<EnemyPays> enemyPayouts;
    private int energyBoss;
    private List<LootboxPrizes> lootboxPrizes;
    private List<SWPaidCosts> weaponPaidMultiplier;
    private Map<Integer, List<Integer>> gemPayouts;
    private Map<Integer, List<Integer>> healthByLevels;
    private Map<Integer, Map<Integer, Prize>> enemyPayoutsByWeapon;
    private Set<Integer> possibleBetLevels;
    private List<Integer> moneyWheelPayouts;
    private Map<Integer, List<Integer>> reels;

    public Paytable() {
    }

    public Paytable(List<EnemyPays> enemyPayouts, int energyBoss, List<LootboxPrizes> lootboxPrizes,
                    List<SWPaidCosts> weaponPaidMultiplier, Map<Integer, List<Integer>> gemPayouts) {
        this.enemyPayouts = enemyPayouts;
        this.energyBoss = energyBoss;
        this.lootboxPrizes = lootboxPrizes;
        this.weaponPaidMultiplier = weaponPaidMultiplier;
        this.gemPayouts = gemPayouts;
        this.healthByLevels = new HashMap<>();
        this.enemyPayoutsByWeapon = new HashMap<>();
        this.possibleBetLevels = new HashSet<>();
        this.moneyWheelPayouts = new ArrayList<>();
    }

    public Paytable(List<EnemyPays> enemyPayouts, int energyBoss, List<LootboxPrizes> lootboxPrizes,
                    List<SWPaidCosts> weaponPaidMultiplier, Map<Integer, List<Integer>> gemPayouts,
                    Map<Integer, List<Integer>> healthByLevels, Map<Integer, Map<Integer, Prize>> enemyPayoutsByWeapon) {
        this.enemyPayouts = enemyPayouts;
        this.energyBoss = energyBoss;
        this.lootboxPrizes = lootboxPrizes;
        this.weaponPaidMultiplier = weaponPaidMultiplier;
        this.gemPayouts = gemPayouts;
        this.healthByLevels = healthByLevels;
        this.enemyPayoutsByWeapon = enemyPayoutsByWeapon;
        this.possibleBetLevels = new HashSet<>();
        this.moneyWheelPayouts = new ArrayList<>();
    }


    public Paytable(List<EnemyPays> enemyPayouts, int energyBoss, List<LootboxPrizes> lootboxPrizes,
                    List<SWPaidCosts> weaponPaidMultiplier, List<Integer> moneyWheelPayouts,
                    Map<Integer, List<Integer>> healthByLevels, Map<Integer, Map<Integer, Prize>> enemyPayoutsByWeapon,
                    Set<Integer> possibleBetLevels) {
        this.enemyPayouts = enemyPayouts;
        this.energyBoss = energyBoss;
        this.lootboxPrizes = lootboxPrizes;
        this.weaponPaidMultiplier = weaponPaidMultiplier;
        this.gemPayouts = new HashMap<>();
        this.healthByLevels = healthByLevels;
        this.enemyPayoutsByWeapon = enemyPayoutsByWeapon;
        this.possibleBetLevels = possibleBetLevels;
        this.moneyWheelPayouts = moneyWheelPayouts;
    }

    public void setHealthByLevels(Map<Integer, List<Integer>> healthByLevels) {
        this.healthByLevels = healthByLevels;
    }

    public Map<Integer, List<Integer>> getHealthByLevels() {
        return healthByLevels;

    }

    public List<EnemyPays> getEnemyPayouts() {
        return enemyPayouts;
    }

    public void setEnemyPayouts(List<EnemyPays> enemyPayouts) {
        this.enemyPayouts = enemyPayouts;
    }

    public int getEnergyBoss() {
        return energyBoss;
    }

    public void setEnergyBoss(int energyBoss) {
        this.energyBoss = energyBoss;
    }

    public List<LootboxPrizes> getLootboxPrizes() {
        return lootboxPrizes;
    }

    public void setLootboxPrizes(List<LootboxPrizes> lootboxPrizes) {
        this.lootboxPrizes = lootboxPrizes;
    }

    public List<SWPaidCosts> getWeaponPaidMultiplier() {
        return weaponPaidMultiplier;
    }

    public void setWeaponPaidMultiplier(List<SWPaidCosts> weaponPaidMultiplier) {
        this.weaponPaidMultiplier = weaponPaidMultiplier;
    }

    public Map<Integer, List<Integer>> getGemPayouts() {
        return gemPayouts;
    }

    public void setGemPayouts(Map<Integer, List<Integer>> gemPayouts) {
        this.gemPayouts = gemPayouts;
    }

    public Map<Integer, Map<Integer, Prize>> getEnemyPayoutsByWeapon() {
        return enemyPayoutsByWeapon;
    }

    public void setEnemyPayoutsByWeapon(Map<Integer, Map<Integer, Prize>> enemyPayoutsByWeapon) {
        this.enemyPayoutsByWeapon = enemyPayoutsByWeapon;
    }

    public Set<Integer> getPossibleBetLevels() {
        return possibleBetLevels;
    }

    public void setPossibleBetLevels(Set<Integer> possibleBetLevels) {
        this.possibleBetLevels = possibleBetLevels;
    }

    public Map<Integer, List<Integer>> getReels() {
        return reels;
    }

    public void setReels(Map<Integer, List<Integer>> reels) {
        this.reels = reels;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, enemyPayouts);
        output.writeInt(energyBoss);
        kryo.writeClassAndObject(output, lootboxPrizes);
        kryo.writeClassAndObject(output, weaponPaidMultiplier);
        kryo.writeClassAndObject(output, gemPayouts);
        kryo.writeClassAndObject(output, healthByLevels);
        kryo.writeClassAndObject(output, enemyPayoutsByWeapon);
        kryo.writeClassAndObject(output, possibleBetLevels);
        kryo.writeClassAndObject(output, moneyWheelPayouts);
        kryo.writeClassAndObject(output, reels);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        enemyPayouts = (List<EnemyPays>) kryo.readClassAndObject(input);
        energyBoss = input.readInt();
        lootboxPrizes = (List<LootboxPrizes>) kryo.readClassAndObject(input);
        weaponPaidMultiplier = (List<SWPaidCosts>) kryo.readClassAndObject(input);
        gemPayouts = (Map<Integer, List<Integer>>) kryo.readClassAndObject(input);
        healthByLevels = (Map<Integer, List<Integer>>) kryo.readClassAndObject(input);
        enemyPayoutsByWeapon = (Map<Integer, Map<Integer, Prize>>) kryo.readClassAndObject(input);
        possibleBetLevels = (Set<Integer>) kryo.readClassAndObject(input);
        moneyWheelPayouts = (List<Integer>) kryo.readClassAndObject(input);
        reels = (Map<Integer, List<Integer>>) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paytable paytable = (Paytable) o;
        return energyBoss == paytable.energyBoss &&
                Objects.equals(enemyPayouts, paytable.enemyPayouts) &&
                Objects.equals(lootboxPrizes, paytable.lootboxPrizes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enemyPayouts, energyBoss, lootboxPrizes);
    }
}
