package com.betsoft.casino.mp.common.scenarios;

import com.betsoft.casino.mp.model.movement.Trajectory;

import java.util.List;

public class SpawnGroup {

    private int minSize;
    private int maxSize;
    private List<TypeAndSkin> enemies;
    private long startTime;
    private long travelTime;
    private int deltaX;
    private int deltaY;
    private double deltaSpeed;
    private List<Integer> trajectoryIds;
    private Trajectory trajectory;
    private TypeAndSkin mainEnemy;
    private int delayTime;
    private boolean needRetinueEnemies;
    private boolean fromPortal;

    public SpawnGroup(int minSize, int maxSize, List<TypeAndSkin> enemyTypesAndSkins,
                      long startTime, long travelTime, int deltaX,
                      int deltaY, double deltaSpeed, List<Integer> trajectoryIds, Trajectory trajectory,
                      TypeAndSkin mainEnemy, int delayTime, boolean needRetinueEnemies, boolean fromPortal) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.enemies = enemyTypesAndSkins;
        this.startTime = startTime;
        this.travelTime = travelTime;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaSpeed = deltaSpeed;
        this.trajectoryIds = trajectoryIds;
        this.trajectory = trajectory;
        this.mainEnemy = mainEnemy;
        this.delayTime = delayTime;
        this.needRetinueEnemies = needRetinueEnemies;
        this.fromPortal = fromPortal;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public List<TypeAndSkin> getEnemies() {
        return enemies;
    }

    public void setEnemyTypesAndSkins(List<TypeAndSkin> enemies) {
        this.enemies = enemies;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(long travelTime) {
        this.travelTime = travelTime;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(int deltaX) {
        this.deltaX = deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    public void setDeltaY(int deltaY) {
        this.deltaY = deltaY;
    }

    public List<Integer> getTrajectoryIds() {
        return trajectoryIds;
    }

    public void setTrajectoryIds(List<Integer> trajectoryIds) {
        this.trajectoryIds = trajectoryIds;
    }

    public Trajectory getTrajectory() {
        return trajectory;
    }

    public void setTrajectory(Trajectory trajectory) {
        this.trajectory = trajectory;
    }

    public double getDeltaSpeed() {
        return deltaSpeed;
    }

    public void setDeltaSpeed(double deltaSpeed) {
        this.deltaSpeed = deltaSpeed;
    }

    public TypeAndSkin getMainEnemy() {
        return mainEnemy;
    }

    public void setMainEnemy(TypeAndSkin mainEnemy) {
        this.mainEnemy = mainEnemy;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public boolean isNeedRetinueEnemies() {
        return needRetinueEnemies;
    }

    public void setNeedRetinueEnemies(boolean needRetinueEnemies) {
        this.needRetinueEnemies = needRetinueEnemies;
    }

    public boolean isFromPortal() {
        return fromPortal;
    }

    public void setFromPortal(boolean fromPortal) {
        this.fromPortal = fromPortal;
    }

    @Override
    public String toString() {
        return "SpawnGroup{" +
                "minSize=" + minSize +
                ", maxSize=" + maxSize +
                ", enemies=" + enemies +
                ", startTime=" + startTime +
                ", travelTime=" + travelTime +
                ", deltaX=" + deltaX +
                ", deltaY=" + deltaY +
                ", deltaSpeed=" + deltaSpeed +
                ", trajectoryIds=" + trajectoryIds +
                ", trajectory=" + trajectory +
                ", mainEnemy=" + mainEnemy +
                ", delayTime=" + delayTime +
                ", needRetinueEnemies=" + needRetinueEnemies +
                ", fromPortal=" + fromPortal +
                '}';
    }
}
