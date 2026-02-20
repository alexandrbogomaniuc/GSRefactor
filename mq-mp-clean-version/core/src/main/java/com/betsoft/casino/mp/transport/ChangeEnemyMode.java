package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.EnemyMode;
import com.betsoft.casino.mp.model.IChangeEnemyMode;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;

public class ChangeEnemyMode extends TObject implements IChangeEnemyMode {
    private long enemyId;
    private int enemyModeId;

    public ChangeEnemyMode(long date, long enemyId, EnemyMode enemyMode) {
        super(date, SERVER_RID);
        this.enemyId = enemyId;
        this.enemyModeId = enemyMode.ordinal();
    }

    @Override
    public long getEnemyId() {
        return enemyId;
    }

    @Override
    public int getEnemyModeId() {
        return enemyModeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChangeEnemyMode that = (ChangeEnemyMode) o;
        return enemyId == that.enemyId &&
                enemyModeId == that.enemyModeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enemyId, enemyModeId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeEnemyMode{");
        sb.append("enemyId=").append(enemyId);
        sb.append(", enemyModeId=").append(enemyModeId);
        sb.append('}');
        return sb.toString();
    }
}
