package com.betsoft.casino.mp.clashofthegods.model.math;

import com.betsoft.casino.mp.common.ISwarmType;
import com.betsoft.casino.mp.clashofthegods.model.math.EnemyType;

public enum SwarmType implements ISwarmType {
    SWARM_PARAMS(1),
    SWARM_SCENARIO(2),
    EVIL_SPIRIT(3),
    LIZARD_MAN(4),
    OWL(5),
    EVIL_SPIRIT_LIZARD_MAN(6),
    EVIL_SPIRIT_LINE(7),
    LIZARD_MAN_LINE(8),
    PHOENIX_LANTERN(9),
    DRAGON_FLY_MODE_1(10),
    DRAGON_FLY_MODE_2(11),
    BEETLE_SWARM_1(12),
    BEETLE_SWARM_2(13);


    private int typeId;

    SwarmType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
