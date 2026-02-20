package com.betsoft.casino.mp.model;

import java.util.Collections;
import java.util.Map;

public interface ICrashRoundInfo {

    double getMult();

    long getStartTime();

    long getRoundId();

    int getBets();

    String getToken();

    String getSalt();

    default Map<String, Double> getWinners(){ return Collections.emptyMap();}

    default double getKilometerMult() {
        return 1.0;
    }
}
