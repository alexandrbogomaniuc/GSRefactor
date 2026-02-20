package com.betsoft.casino.mp.common.scenarios;

import java.util.List;

public class SpawnScenario {

    private int id;
    private long cooldown;
    private int offsetX;
    private int offsetY;
    private List<Integer> trajectoryIds;
    private final List<SpawnGroup> groups;

    public SpawnScenario(int id, long cooldown, int offsetX, int offsetY, List<Integer> trajectoryIds, List<SpawnGroup> groups) {
        this.id = id;
        this.cooldown = cooldown;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.trajectoryIds = trajectoryIds;
        this.groups = groups;
    }

    public int getId() {
        return id;
    }

    public long getCooldown() {
        return cooldown;
    }

    public List<Integer> getTrajectoryIds() {
        return trajectoryIds;
    }

    public List<SpawnGroup> getGroups() {
        return groups;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }
}
