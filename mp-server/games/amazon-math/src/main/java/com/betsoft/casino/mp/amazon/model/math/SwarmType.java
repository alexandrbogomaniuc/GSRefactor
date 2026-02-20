package com.betsoft.casino.mp.amazon.model.math;

import com.betsoft.casino.mp.common.ISwarmType;

public enum SwarmType implements ISwarmType {
    TRIPLE_SNAKE(1),
    ANT_SCENARIO(2),
    ANT_SINGLE_FILE(3),
    WASP_REGULAR(4),
    WASP_ORION(5),
    RUNNERS(6);

    private int typeId;

    SwarmType(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public int getTypeId() {
        return typeId;
    }
}
