package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IEnemyMode;

import java.util.Objects;

public class GameEnemyMode implements IEnemyMode {

    private long enemyId;
    private int enemyMode;

    public GameEnemyMode(long enemyId, int enemyMode) {
        this.enemyId = enemyId;
        this.enemyMode = enemyMode;
    }

    @Override
    public long getEnemyId() {
        return enemyId;
    }

    @Override
    public int getEnemyMode() {
        return enemyMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameEnemyMode that = (GameEnemyMode) o;
        return enemyId == that.enemyId && enemyMode == that.enemyMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enemyId, enemyMode);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameEnemyMode[");
        sb.append("enemyId=").append(enemyId);
        sb.append(", enemyMode=").append(enemyMode);
        sb.append(']');
        return sb.toString();
    }
}
