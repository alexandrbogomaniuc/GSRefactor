package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.common.util.RNG;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * User: flsh
 * Date: 12.07.18.
 */
public enum AvatarParts {
    BORDER(4, 0, Arrays.asList(0, 1, 2, 3, 4)),
    HERO(4, 0, Arrays.asList(0, 1, 2, 3, 4)),
    BACKGROUND(5, 0, Arrays.asList(0, 1, 2, 3, 4, 5));

    private int maxPartId;
    private int defaultPartId;
    private List<Integer> freeParts;

    AvatarParts(int maxPartId, int defaultPartId, List<Integer> freeParts) {
        this.maxPartId = maxPartId;
        this.defaultPartId = defaultPartId;
        this.freeParts = freeParts;
    }

    public int getMaxPartId() {
        return maxPartId;
    }

    public List<Integer> getFreeParts() {
        return freeParts;
    }

    public int getDefaultPartId() {
        return RNG.nextInt(freeParts.size());
    }

    public boolean isPartAvailable(Set<Integer> purchased, Integer part) {
        return purchased.contains(part) || freeParts.contains(part);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AvatarParts [");
        sb.append("maxPartId=").append(maxPartId);
        sb.append(", freeParts=").append(freeParts);
        sb.append(']');
        return sb.toString();
    }
}
