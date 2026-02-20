package com.betsoft.casino.mp.model.gameconfig;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Map;

public class EnemyParams implements KryoSerializable {
    protected static final byte VERSION = 0;
    int health;
    Map<String, Map<Double, Double>> damage_probability;
    Map<String, Double> wins;

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Map<String, Map<Double, Double>> getDamage_probability() {
        return damage_probability;
    }

    public void setDamage_probability(Map<String, Map<Double, Double>> damage_probability) {
        this.damage_probability = damage_probability;
    }

    public Map<String, Double> getWins() {
        return wins;
    }

    public void setWins(Map<String, Double> wins) {
        this.wins = wins;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyParams{");
        sb.append("health=").append(health);
        sb.append(", damage_probability=").append(damage_probability);
        sb.append(", wins=").append(wins);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(health);
        kryo.writeClassAndObject(output, damage_probability);
        kryo.writeClassAndObject(output, wins);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        input.readByte();
        health = input.readInt();
        damage_probability = (Map<String, Map<Double, Double>>) kryo.readClassAndObject(input);
        wins = (Map<String, Double>) kryo.readClassAndObject(input);
    }
}
