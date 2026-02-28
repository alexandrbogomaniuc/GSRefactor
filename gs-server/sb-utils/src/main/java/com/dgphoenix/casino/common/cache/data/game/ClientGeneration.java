package com.abs.casino.common.cache.data.game;

public enum ClientGeneration {
    UNDEFINED(-1),
    //First 'Unified Engine' implementation: '/html5/u_[gamename]' for BSG; '/html5/u__[gamename]' for Nucleus
    UNIFIED_ENGINE(0),
    //'Responsive Design' based on UE: '/html5/u_rd_[gamename]' for BSG; '/html5/u_rd__[gamename]' for Nucleus
    RESPONSIVE_DESIGN(1),
    //'Responsive Design' v2 based on UE: '/html5/u_rd_2_[gamename]' for BSG; '/html5/u_rd_2__[gamename]' for Nucleus
    RESPONSIVE_DESIGN_V2(2);

    private final int generation;

    public int getGeneration() {
        return generation;
    }

    ClientGeneration(int generation) {
        this.generation = generation;
    }
}
