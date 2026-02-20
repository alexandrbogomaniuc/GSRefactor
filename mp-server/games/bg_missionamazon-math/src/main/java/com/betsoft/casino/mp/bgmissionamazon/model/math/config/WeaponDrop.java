package com.betsoft.casino.mp.bgmissionamazon.model.math.config;

import com.betsoft.casino.mp.model.IWeight;
import com.betsoft.casino.mp.model.SpecialWeaponType;

public class WeaponDrop implements IWeight {
    private SpecialWeaponType type;
    private int amount;
    private int weight;

    public WeaponDrop(SpecialWeaponType type, int amount, int weight) {
        this.type = type;
        this.amount = amount;
        this.weight = weight;
    }

    public SpecialWeaponType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "WeaponDrop" + "[" +
                "type=" + type +
                ", amount=" + amount +
                ", weight=" + weight +
                ']';
    }
}
