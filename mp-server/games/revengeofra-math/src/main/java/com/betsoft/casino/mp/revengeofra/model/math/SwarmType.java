package com.betsoft.casino.mp.revengeofra.model.math;

import com.betsoft.casino.mp.common.ISwarmType;
import com.betsoft.casino.mp.revengeofra.model.math.EnemyType;

public enum SwarmType implements ISwarmType {
    SWARM_PARAMS(1),
    LOCUST(5),
    SWARM_SCENARIO(2),
    DUAL_SPEED_MUMMIES(3),
    PHARAON_MUMMIES(4),
    SMALL_GROUP_MUMMIES(5);

    private int typeId;

    SwarmType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
