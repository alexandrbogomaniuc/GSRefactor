package com.betsoft.casino.mp;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;

public class WizardTrajectoryGenerator extends ShamanTrajectoryGenerator {
    private static final String WIZARD_POINTS = "2";
    private final Coords coords;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private final List<PointD> wizardPoints;
    private final List<AbstractEnemy> currentWizards;

    public WizardTrajectoryGenerator(GameMapShape map, PointI source, double speed, Coords coords,
                                     List<AbstractEnemy> currentWizards) {
        super(map, source, speed);
        this.coords = coords;
        wizardPoints = map.getPoints(WIZARD_POINTS);
        this.currentWizards = currentWizards;
    }

    public WizardTrajectoryGenerator setVisibleArea(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        return this;
    }

    @Override
    protected PointD getRandomTeleportPoint() {
        while (true) {
            int x = RNG.nextInt(map.getWidth());
            int y = RNG.nextInt(map.getHeight());
            if (map.isValid(x, y) && map.isNotMarked(x, y) && isVisible(x, y)) {
                return new PointD(x, y);
            }
        }
    }

    private boolean isVisible(int x, int y) {
        double screenX = coords.toScreenX(x, y);
        double screenY = coords.toScreenY(x, y);
        return minX <= screenX && screenX <= maxX && minY <= screenY && screenY <= maxY;
    }

    @Override
    public Trajectory generate(long startTime, int minSteps, boolean needFinalSteps) {
        trajectory = new Trajectory(speed);
        time = startTime;
        firstStep();
        for (int i = 0; i < minSteps; i++) {
            randomStep();
        }
        if (needFinalSteps) {
            finalSteps();
        }
        return trajectory;
    }

    @Override
    protected boolean firstStep() {
        randomStep();
        return true;
    }

    @Override
    protected void randomStep() {
        PointD point = getRandomElement(wizardPoints);
        while (!checkPointAvailability(point)) {
            point = getRandomElement(wizardPoints);
        }
        addTeleportPoint(point);
    }

    protected boolean checkPointAvailability(PointD point) {
        for (AbstractEnemy wizard : currentWizards) {
            if (PointD.equals(wizard.getLocation(time), point, 0.1)
                    || PointD.equals(wizard.getLocation(time + 7000), point, 0.1)
                    || PointD.equals(wizard.getLocation(time + 10000), point, 0.1)) {
                return false;
            }
        }
        return true;
    }

    protected <T> T getRandomElement(List<T> list) {
        return list.get(RNG.nextInt(list.size()));
    }
}
