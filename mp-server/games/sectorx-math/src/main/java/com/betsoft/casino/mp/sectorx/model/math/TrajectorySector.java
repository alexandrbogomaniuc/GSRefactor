package com.betsoft.casino.mp.sectorx.model.math;

public enum TrajectorySector {
    HGROUPI(1),
    HGROUPII(2),
    HGROUPIII(3),
    HGROUPIV(4);

    private int typeId;

    public int getTypeId() {
        return typeId;
    }

    TrajectorySector(int typeId) {
        this.typeId = typeId;
    }
}
