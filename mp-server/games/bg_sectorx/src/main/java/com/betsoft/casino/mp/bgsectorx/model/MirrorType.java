package com.betsoft.casino.mp.bgsectorx.model;

public enum MirrorType {
    X(1),
    Y(2),
    DOff(3),
    D(4);
    private int typeId;

    MirrorType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return this.typeId;
    }
}
