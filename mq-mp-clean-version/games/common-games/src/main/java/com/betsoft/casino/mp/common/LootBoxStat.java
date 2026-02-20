package com.betsoft.casino.mp.common;

import com.dgphoenix.casino.common.util.Pair;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LootBoxStat implements KryoSerializable {
    private static final byte VERSION = 0;
    private int cost;
    private Map<String, Pair<Integer, Integer>> weaponShots;

    public LootBoxStat() {}

    public LootBoxStat(int cost) {
        this.cost = cost;
        this.weaponShots = new HashMap<>();
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public Map<String, Pair<Integer, Integer>> getWeaponShots() {
        return weaponShots;
    }

    public void setWeaponShots(Map<String, Pair<Integer, Integer>> weaponShots) {
        this.weaponShots = weaponShots;
    }

    public void addWeaponShots(String weaponName, int shots) {
        Pair<Integer, Integer> weaponShotsOrDefault = weaponShots.getOrDefault(weaponName, new Pair<>(0, 0));
        weaponShots.put(weaponName, new Pair<>(weaponShotsOrDefault.getKey() + 1,  weaponShotsOrDefault.getValue() + shots));
    }

    public void addWeaponShots(String weaponName, int shots, int numberByIn) {
        Pair<Integer, Integer> weaponShotsOrDefault = weaponShots.getOrDefault(weaponName, new Pair<>(0, 0));
        weaponShots.put(weaponName, new Pair<>(weaponShotsOrDefault.getKey() + numberByIn,  weaponShotsOrDefault.getValue() + shots));
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(cost, true);
        kryo.writeClassAndObject(output, weaponShots);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        cost = input.readInt(true);
        weaponShots = (Map<String, Pair<Integer, Integer>>) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootBoxStat that = (LootBoxStat) o;
        return cost == that.cost &&
                Objects.equals(weaponShots, that.weaponShots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost, weaponShots);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LootBoxStat{");
        sb.append("cost=").append(cost);
        sb.append(", weaponShots=").append(weaponShots);
        sb.append('}');
        return sb.toString();
    }
}
