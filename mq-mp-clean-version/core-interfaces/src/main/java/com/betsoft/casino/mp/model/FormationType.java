package com.betsoft.casino.mp.model;

public enum FormationType {
    TEMPORAL(1),
    SPATIAL(2),
    CLUSTER(3),
    HYBRID(4);
    private int typeId;

    FormationType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return this.typeId;
    }
}