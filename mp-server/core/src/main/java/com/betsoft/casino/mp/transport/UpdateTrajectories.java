package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IUpdateTrajectories;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.utils.TObject;

import java.util.Map;

public class UpdateTrajectories extends TObject implements IUpdateTrajectories {
    private Map<Long, Trajectory> trajectories;
    private int freezeTime;
    private int animationId;

    public UpdateTrajectories(long date, int rid, Map<Long, Trajectory> trajectories, int freezeTime, int animationId) {
        super(date, rid);
        this.trajectories = trajectories;
        this.freezeTime = freezeTime;
        this.animationId = animationId;
    }

    @Override
    public Map<Long, Trajectory> getTrajectories() {
        return trajectories;
    }

    @Override
    public int getFreezeTime() {
        return freezeTime;
    }

    @Override
    public int getAnimationId() {
        return animationId;
    }

    @Override
    public String toString() {
        return "UpdateTrajectories[" +
                "trajectories=" + trajectories +
                ", date=" + date +
                ", freezeTime=" + freezeTime +
                ", animationId=" + animationId +
                ", rid=" + rid +
                ']';
    }
}
