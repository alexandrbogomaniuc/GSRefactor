package com.betsoft.casino.mp.common.scenarios;

public class TypeAndSkin {

    private final int type;
    private final int skin;

    public TypeAndSkin(int type, int skin) {
        this.type = type;
        this.skin = skin;
    }

    public int getType() {
        return type;
    }

    public int getSkin() {
        return skin;
    }

    @Override
    public String toString() {
        return "TypeAndSkin{" +
                "type=" + type +
                ", skin=" + skin +
                '}';
    }
}
