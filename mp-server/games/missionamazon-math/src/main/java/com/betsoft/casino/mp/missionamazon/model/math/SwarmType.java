package com.betsoft.casino.mp.missionamazon.model.math;

import com.betsoft.casino.mp.common.ISwarmType;

public enum SwarmType implements ISwarmType {
    SNAKES(1),
    ANTS(2),
    ANT_SINGLE_FILE(3),
    WASP_REGULAR(4),
    WASP_ORION(5),
    RUNNERS(6),
    FROGS(7);

    private int typeId;

    SwarmType(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public int getTypeId() {
        return typeId;
    }
}
