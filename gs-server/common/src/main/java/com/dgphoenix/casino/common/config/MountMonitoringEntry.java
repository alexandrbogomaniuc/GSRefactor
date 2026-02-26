package com.abs.casino.common.config;

import com.abs.casino.common.config.FreeSpaceThresholdType;

/**
 * Created by vladislav on 05/05/15.
 */
public class MountMonitoringEntry {
    private final String mountPath;
    private final FreeSpaceThresholdType thresholdType;
    private final long thresholdAmount;

    public String getMountPath() {
        return mountPath;
    }

    public MountMonitoringEntry(String mountPath, FreeSpaceThresholdType thresholdType, long thresholdAmount) {
        this.mountPath = mountPath;
        this.thresholdType = thresholdType;
        this.thresholdAmount = thresholdAmount;
    }

    public FreeSpaceThresholdType getThresholdType() {
        return thresholdType;
    }

    public long getThresholdAmount() {
        return thresholdAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MountMonitoringEntry that = (MountMonitoringEntry) o;

        if (!mountPath.equals(that.mountPath)) return false;
        return thresholdType == that.thresholdType;
    }

    @Override
    public int hashCode() {
        int result = mountPath.hashCode();
        result = 31 * result + thresholdType.hashCode();
        return result;
    }
}
