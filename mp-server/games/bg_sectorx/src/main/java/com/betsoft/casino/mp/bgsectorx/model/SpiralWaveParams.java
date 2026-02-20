package com.betsoft.casino.mp.bgsectorx.model;

import com.betsoft.casino.mp.bgsectorx.model.math.EnemyType;
import com.betsoft.casino.mp.bgsectorx.model.math.InitialWaveType;

public class SpiralWaveParams {
    private EnemyType enemyType;
    private InitialWaveType typeSpiralWave;
    private double enemySpeed;
    private long pathTime;
    private boolean isFirstSpawn;
    private int countWaves;

    public SpiralWaveParams(EnemyType enemyType, InitialWaveType typeSpiralWave, double enemySpeed, long pathTime, int countWaves) {
        this.enemyType = enemyType;
        this.typeSpiralWave = typeSpiralWave;
        this.enemySpeed = enemySpeed;
        this.pathTime = pathTime;
        this.countWaves = countWaves;
    }

    public SpiralWaveParams() {
        isFirstSpawn = true;
    }

    public EnemyType getEnemyType() {
        return enemyType;
    }

    public void setEnemyType(EnemyType enemyType) {
        this.enemyType = enemyType;
    }

    public InitialWaveType getTypeSpiralWave() {
        return typeSpiralWave;
    }

    public void setTypeSpiralWave(InitialWaveType typeSpiralWave) {
        this.typeSpiralWave = typeSpiralWave;
    }

    public boolean isFirstSpawn() {
        return isFirstSpawn;
    }

    public void setFirstSpawn(boolean firstSpawn) {
        isFirstSpawn = firstSpawn;
    }

    public double getEnemySpeed() {
        return enemySpeed;
    }

    public void setEnemySpeed(double enemySpeed) {
        this.enemySpeed = enemySpeed;
    }

    public long getPathTime() {
        return pathTime;
    }

    public void setPathTime(long pathTime) {
        this.pathTime = pathTime;
    }

    public int getCountWaves() {
        return countWaves;
    }

    public void setCountWaves(int countWaves) {
        this.countWaves = countWaves;
    }
}
