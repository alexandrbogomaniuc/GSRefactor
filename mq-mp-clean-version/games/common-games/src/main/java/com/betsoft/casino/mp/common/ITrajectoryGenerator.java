package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.movement.Trajectory;

public interface ITrajectoryGenerator {

    Trajectory generate(int x, int y, long startTime, double speed);
    Trajectory generate(int x, int y, long startTime, long endTime, double minSpeed);

}
