package com.betsoft.casino.mp.common.math;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.Objects;

public class EnemyPays  implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private int idEnemy;
    private String nameEnemy;
    private Prize prize;

    public EnemyPays() {}

    public EnemyPays(int idEnemy, String nameEnemy, Prize prize) {
        this.idEnemy = idEnemy;
        this.nameEnemy = nameEnemy;
        this.prize = prize;
    }

    public int getIdEnemy() {
        return idEnemy;
    }

    public void setIdEnemy(int idEnemy) {
        this.idEnemy = idEnemy;
    }

    public String getNameEnemy() {
        return nameEnemy;
    }

    public void setNameEnemy(String nameEnemy) {
        this.nameEnemy = nameEnemy;
    }

    public Prize getPrize() {
        return prize;
    }

    public void setPrize(Prize prize) {
        this.prize = prize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnemyPays enemyPays = (EnemyPays) o;
        return idEnemy == enemyPays.idEnemy &&
                Objects.equals(nameEnemy, enemyPays.nameEnemy) &&
                Objects.equals(prize, enemyPays.prize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEnemy, nameEnemy, prize);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(idEnemy, true);
        output.writeString(nameEnemy);
        kryo.writeClassAndObject(output, prize);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        idEnemy = input.readInt(true);
        nameEnemy = input.readString();
        prize = (Prize) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "EnemyPays{" +
                "idEnemy=" + idEnemy +
                ", nameEnemy='" + nameEnemy + '\'' +
                ", prize=" + prize +
                '}';
    }
}
