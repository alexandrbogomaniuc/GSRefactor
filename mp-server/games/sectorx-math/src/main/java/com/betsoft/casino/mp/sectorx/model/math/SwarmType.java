package com.betsoft.casino.mp.sectorx.model.math;

import com.betsoft.casino.mp.common.ISwarmType;

public enum SwarmType implements ISwarmType {
    Temporal(1),
    Spatial(2),
    Cluster(3),
    Hybrid(4),
    HugeEnemy(5);

    private int typeId;

    SwarmType(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public int getTypeId() {
        return typeId;
    }
}
