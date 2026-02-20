package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IWeapon;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class Weapon implements KryoSerializable, Serializable, IWeapon {
    private static final byte VERSION = 0;

    private int shots;
    private SpecialWeaponType type;

    public Weapon() {}

    public Weapon(int shots, SpecialWeaponType type) {
        this.shots = shots;
        this.type = type;
    }

    @Override
    public Weapon clone() {
        return new Weapon(shots, type);
    }

    @Override
    public int getShots() {
        return shots;
    }

    @Override
    public void setShots(int shots) {
        this.shots = shots;
    }

    @Override
    public void addShots(int shots) {
        this.shots += shots;
    }

    @Override
    public SpecialWeaponType getType() {
        return type;
    }

    public void setType(SpecialWeaponType type) {
        this.type = type;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(type.getId(), true);
        output.writeInt(shots, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        type = SpecialWeaponType.values()[input.readInt(true)];
        shots = input.readInt(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Weapon[");
        sb.append("shots=").append(shots);
        sb.append(", type=").append(type);
        sb.append(']');
        return sb.toString();
    }
}
