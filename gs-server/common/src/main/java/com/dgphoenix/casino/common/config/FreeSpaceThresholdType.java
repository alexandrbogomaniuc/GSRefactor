package com.abs.casino.common.config;

/**
 * Created by vladislav on 12/28/17.
 */
public enum FreeSpaceThresholdType {
    MEGABYTES {
        @Override
        public long calculateFreeSpace(long totalSpaceInMB, long freeSpaceInMB) {
            return freeSpaceInMB;
        }
    },
    PERCENTAGE {
        @Override
        public long calculateFreeSpace(long totalSpaceInMB, long freeSpaceInMB) {
            return Math.round(((double) freeSpaceInMB / totalSpaceInMB) * 100);
        }
    };

    public boolean isFreeSpaceMuchLower(long thresholdAmount, long totalSpaceInMB, long freeSpaceInMB) {
        return calculateFreeSpace(totalSpaceInMB, freeSpaceInMB) < thresholdAmount / 2;
    }

    public boolean isFreeSpaceLower(long thresholdAmount, long totalSpaceInMB, long freeSpaceInMB) {
        return calculateFreeSpace(totalSpaceInMB, freeSpaceInMB) < thresholdAmount;
    }

    public abstract long calculateFreeSpace(long totalSpaceInMB, long freeSpaceInMB);
}
