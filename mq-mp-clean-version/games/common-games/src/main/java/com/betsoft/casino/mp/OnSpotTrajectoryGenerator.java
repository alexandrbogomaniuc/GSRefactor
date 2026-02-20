package com.betsoft.casino.mp;

import com.betsoft.casino.mp.common.TrajectoryGenerator;
import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.kynosarges.tektosyne.geometry.PointI;
import org.kynosarges.tektosyne.geometry.PointD;


public class OnSpotTrajectoryGenerator extends TrajectoryGenerator {

    private final PointD base;
    private final double radius;

    public OnSpotTrajectoryGenerator(IGameMapShape map, PointI source, double speed, PointD base, double radius) {
        super(map, source, speed);
        this.base = base;
        this.radius = radius;
    }

    @Override
    public Trajectory generate(Trajectory trajectory, long startTime, int duration, boolean needFinalSteps) {
        this.trajectory = trajectory;
        time = startTime;
        int maxSteps = 50;
        while (maxSteps-- > 0 && time < startTime + duration) {
             randomStep();
        }
        if (needFinalSteps) {
            finalSteps();
        }
        return trajectory;
    }

    @Override
    public int getMaxDistanceToBorder(int x, int y, int dx, int dy) {
        if (!map.isValid(x, y)) {
            return 0;
        }
        int result = 0;
        int nx = x + dx;
        int ny = y + dy;
        while (nx >= 0 && ny >= 0 && nx < map.getWidth() && ny < map.getHeight() && map.isPassable(nx, ny)
                && !map.isBorder(nx, ny) && getDistance(base.x, base.y, nx, ny) <= radius) {
            result++;
            nx += dx;
            ny += dy;
        }
        return result;
    }

    private double getDistance(double ax, double ay, double bx, double by) {
        double dx = Math.abs(bx - ax);
        double dy = Math.abs(by - ay);
        return Math.sqrt(dx * dx + dy * dy);
    }
}
