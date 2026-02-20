package com.betsoft.casino.mp.sectorx.model;

public enum SideMap {
    NORTH(1),
    SOUTH(2),
    EAST(3),
    WEST(4);
    private int typeId;

    SideMap(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return this.typeId;
    }
}
