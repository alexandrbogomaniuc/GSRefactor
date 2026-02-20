package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Trajectory;

import java.util.List;

public class GroupParams {
    private int type;
    private int minSize;
    private int maxSize;
    private boolean canStop;
    private List<GroupTemplate> templates;
    private List<Trajectory> trajectories;

    public GroupParams(int type, int minSize, int maxSize, boolean canStop, List<GroupTemplate> templates, List<Trajectory> trajectories) {
        this.type = type;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.canStop = canStop;
        this.templates = templates;
        this.trajectories = trajectories;
    }

    public int getType() {
        return type;
    }

    public int getMinSize() {
        return minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isCanStop() {
        return canStop;
    }

    public List<GroupTemplate> getTemplates() {
        return templates;
    }

    public List<Trajectory> getTrajectories() {
        return trajectories;
    }

    @Override
    public String toString() {
        return "GroupParams{" +
                "type=" + type +
                ", minSize=" + minSize +
                ", maxSize=" + maxSize +
                ", canStop=" + canStop +
                ", templates=" + templates +
                ", trajectories=" + trajectories +
                '}';
    }
}
