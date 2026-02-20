package com.betsoft.casino.mp.common.math;

import com.betsoft.casino.mp.model.IMathEnemy;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class MathEnemy implements IMathEnemy, KryoSerializable {
    protected static final byte VERSION = 0;
    int index;
    String typeName;
    int settingsEnemyId;
    double fullEnergy;

    public MathEnemy() {}

    public MathEnemy(int index, String typeName, int settingsEnemyId, double fullEnergy) {
        this.index = index;
        this.typeName = typeName;
        this.settingsEnemyId = settingsEnemyId;
        this.fullEnergy = fullEnergy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathEnemy mathEnemy = (MathEnemy) o;
        return index == mathEnemy.index &&
                settingsEnemyId == mathEnemy.settingsEnemyId &&
                Objects.equals(typeName, mathEnemy.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, typeName, settingsEnemyId);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public int getSettingsEnemyId() {
        return settingsEnemyId;
    }

    @Override
    public void setSettingsEnemyId(int settingsEnemyId) {
        this.settingsEnemyId = settingsEnemyId;
    }

    @Override
    public double getFullEnergy() {
        return fullEnergy;
    }

    @Override
    public void setFullEnergy(double fullEnergy) {
        this.fullEnergy = fullEnergy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MathEnemy{");
        sb.append("index=").append(index);
        sb.append(", typeName='").append(typeName).append('\'');
        sb.append(", settingsEnemyId=").append(settingsEnemyId);
        sb.append(", fullEnergy=").append(fullEnergy);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(index, true);
        output.writeString(typeName);
        output.writeInt(settingsEnemyId, true);
        output.writeDouble(fullEnergy);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        index = input.readInt(true);
        typeName = input.readString();
        settingsEnemyId = input.readInt(true);
        fullEnergy = input.readDouble();
    }
}
