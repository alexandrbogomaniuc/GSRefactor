package com.betsoft.casino.mp.piratespov.model;

import com.betsoft.casino.mp.common.ISwarmType;

public enum SwarmType implements ISwarmType {
    RATS(1),
    WHITE_BIRDS(2),
    RED_BIRDS(3),
    CRABS(4);

    private int typeId;

    SwarmType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
