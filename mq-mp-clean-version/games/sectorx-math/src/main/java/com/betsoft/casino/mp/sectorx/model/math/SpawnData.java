package com.betsoft.casino.mp.sectorx.model.math;

public class SpawnData {
    private final int minTimeOffset;
    private final int maxTimeOffset;
    private final int minWaitTime;
    private final int maxWaitTime;
    private final int minStayTime;
    private final int maxStayTime;
    private final boolean unconditionalRespawn;

    public SpawnData(int minTimeOffset, int maxTimeOffset, int minWaitTime, int maxWaitTime,
                     int minStayTime, int maxStayTime, boolean unconditionalRespawn) {
        this.minTimeOffset = minTimeOffset;
        this.maxTimeOffset = maxTimeOffset;
        this.minWaitTime = minWaitTime;
        this.maxWaitTime = maxWaitTime;
        this.minStayTime = minStayTime;
        this.maxStayTime = maxStayTime;
        this.unconditionalRespawn = unconditionalRespawn;
    }

    public int getMinTimeOffset() {
        return minTimeOffset;
    }

    public int getMaxTimeOffset() {
        return maxTimeOffset;
    }

    public int getMinWaitTime() {
        return minWaitTime;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public int getMinStayTime() {
        return minStayTime;
    }

    public int getMaxStayTime() {
        return maxStayTime;
    }

    public boolean isUnconditionalRespawn() {
        return unconditionalRespawn;
    }

    @Override
    public String toString() {
        return "SwarmData" + "[" +
                "minTimeOffset=" + minTimeOffset +
                ", maxTimeOffset=" + maxTimeOffset +
                ", minWaitTime=" + minWaitTime +
                ", maxWaitTime=" + maxWaitTime +
                ", unconditionalRespawn=" + unconditionalRespawn +
                ']';
    }
}
