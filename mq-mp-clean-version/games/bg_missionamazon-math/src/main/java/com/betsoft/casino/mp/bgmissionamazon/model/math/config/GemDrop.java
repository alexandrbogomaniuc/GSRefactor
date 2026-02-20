package com.betsoft.casino.mp.bgmissionamazon.model.math.config;

import com.betsoft.casino.mp.model.IWeight;

public class GemDrop implements IWeight {
    private int type;
    private int prize;
    private int weight;

    public GemDrop(int type, int prize, int weight) {
        this.type = type;
        this.prize = prize;
        this.weight = weight;
    }

    public int getType() {
        return type;
    }

    public int getPrize() {
        return prize;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "GemDrop" + "[" +
                "type=" + type +
                ", prize=" + prize +
                ", weight=" + weight +
                ']';
    }
}
