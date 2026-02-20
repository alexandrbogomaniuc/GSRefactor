package com.betsoft.casino.mp.model;

public interface ICrashBetInfo {
    long getCrashBetAmount();

    void setCrashBetAmount(long crashBetAmount);

    double getMultiplier();

    void setMultiplier(double multiplier);

    boolean isAutoPlay();

    void setAutoPlay(boolean autoPlay);

    boolean isEjected();

    void setEjected(boolean ejected);

    long getEjectTime();

    void setEjectTime(long time);

    Double getAutoPlayMultiplier();

    void setAutoPlayMultiplier(Double autoPlayMultiplier);

    boolean isReserved();

    void setReserved(boolean reserved);
}
