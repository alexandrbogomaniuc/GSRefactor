package com.betsoft.casino.mp.piratesdmc.model;

import com.betsoft.casino.mp.common.ISwarmType;

public enum SwarmType implements ISwarmType {
    RACING_RATS(1),
    WHITE_BIRDS(2),
    RED_BIRDS(3),
    DECKHANDS(4),
    NECKBEARDS(5);

    private int typeId;

    SwarmType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
