package com.betsoft.casino.mp.bgdragonstone.model;

import com.betsoft.casino.mp.common.ISwarmType;

public enum SwarmType implements ISwarmType {
    GOBLINS(1),
    SPIDERS(2),
    BATS(3),
    RATS(5),
    RAVENS(6),
    SKELETONS(7),
    ORCS(8),
    EMPTY_ARMORS(9),
    ANGLE_SPIDERS(10),
    ORC_PLATOON(11),
    IMPS(12);

    private int typeId;

    SwarmType(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public int getTypeId() {
        return typeId;
    }
}
