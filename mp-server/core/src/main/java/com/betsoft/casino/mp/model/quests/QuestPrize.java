package com.betsoft.casino.mp.model.quests;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.Serializable;
import java.util.Objects;

public class QuestPrize implements IQuestPrize<QuestAmount>, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private QuestAmount amount;
    private int specialWeaponId;

    public QuestPrize() {}

    public QuestPrize(QuestAmount amount, int specialWeaponId) {
        this.amount = amount;
        this.specialWeaponId = specialWeaponId;
    }

    public static QuestPrize convert(IQuestPrize prize) {
        return new QuestPrize(QuestAmount.convert(prize.getAmount()), prize.getSpecialWeaponId());
    }

    @Override
    public QuestAmount getAmount() {
        return amount;
    }

    @Override
    public void setAmount(QuestAmount amount) {
        this.amount = amount;
    }

    @Override
    public int getSpecialWeaponId() {
        return specialWeaponId;
    }

    @Override
    public void setSpecialWeaponId(int specialWeaponId) {
        this.specialWeaponId = specialWeaponId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestPrize that = (QuestPrize) o;
        return amount == that.amount &&
                specialWeaponId == that.specialWeaponId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, specialWeaponId);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, amount);
        output.writeInt(specialWeaponId, true);
    }


    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        amount = (QuestAmount) kryo.readClassAndObject(input);
        specialWeaponId = input.readInt(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Prize[");
        sb.append("amount=").append(amount);
        sb.append(", specialWeaponId=").append(specialWeaponId);
        sb.append(']');
        return sb.toString();
    }
}
