package com.betsoft.casino.mp.model.gameconfig;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class BossPartEnemy implements KryoSerializable {
    protected static final byte VERSION = 0;
    EnemyParams enemyParams;
    double currentHealth;

    public BossPartEnemy() {}

    public BossPartEnemy(EnemyParams enemyParams) {
        this.enemyParams = enemyParams;
        this.currentHealth = enemyParams.getHealth();
    }

    public EnemyParams getEnemyParams() {
        return enemyParams;
    }

    public double getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(double currentHealth) {
        this.currentHealth = currentHealth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BossPartEnemy that = (BossPartEnemy) o;
        return Double.compare(that.currentHealth, currentHealth) == 0 &&
                Objects.equals(enemyParams, that.enemyParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enemyParams, currentHealth);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BossPartEnemy{");
        sb.append("enemyParams=").append(enemyParams);
        sb.append(", currentHealth=").append(currentHealth);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, enemyParams);
        output.writeDouble(currentHealth);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        input.readByte();
        enemyParams = (EnemyParams) kryo.readClassAndObject(input);
        currentHealth = input.readDouble();
    }
}
