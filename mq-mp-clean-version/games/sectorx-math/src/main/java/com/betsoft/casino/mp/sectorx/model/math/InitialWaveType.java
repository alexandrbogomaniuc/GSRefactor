package com.betsoft.casino.mp.sectorx.model.math;

public enum InitialWaveType {
    SpiralWave1(0),
    SpiralWave2(1),
    SpecialPattern(2),
    CrossPaths1(3),
    CrossPaths2(4);

    private int typeId;

    InitialWaveType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
