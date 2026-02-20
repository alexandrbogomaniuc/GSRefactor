package com.betsoft.casino.mp.bgdragonstone.model.math.config;

public class WeaponDrop {
    private int type;
    private int amount;
    private int weight;

    public WeaponDrop(int type, int amount, int weight) {
        this.type = type;
        this.amount = amount;
        this.weight = weight;
    }

    public int getType() {
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
        return "WeaponDropEntry{" +
                "type=" + type +
                ", amount=" + amount +
                ", weight=" + weight +
                '}';
    }
}
