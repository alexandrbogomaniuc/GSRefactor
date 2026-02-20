package com.dgphoenix.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class MQuestPrize  implements KryoSerializable {
    private static final byte VERSION = 0;
    private MQuestAmount amount;
    private int specialWeaponId;

    public MQuestPrize() {}

    public MQuestPrize(MQuestAmount amount, int specialWeaponId) {
        this.amount = amount;
        this.specialWeaponId = specialWeaponId;
    }

    public MQuestAmount getAmount() {
        return amount;
    }

    public void setAmount(MQuestAmount amount) {
        this.amount = amount;
    }

    public int getSpecialWeaponId() {
        return specialWeaponId;
    }

    public void setSpecialWeaponId(int specialWeaponId) {
        this.specialWeaponId = specialWeaponId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output,amount);
        output.writeInt(specialWeaponId, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        amount = (MQuestAmount) kryo.readClassAndObject(input);
        specialWeaponId = input.readInt(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MQuestPrize{");
        sb.append("amount=").append(amount);
        sb.append(", specialWeaponId=").append(specialWeaponId);
        sb.append('}');
        return sb.toString();
    }
}
