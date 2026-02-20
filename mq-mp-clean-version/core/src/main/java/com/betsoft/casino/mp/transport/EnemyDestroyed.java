package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IEnemyDestroyed;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;

/**
 * User: flsh
 * Date: 24.03.18.
 */
public class EnemyDestroyed extends TObject implements IEnemyDestroyed {
    private long enemyId;
    private int reason;

    public EnemyDestroyed(long date, int rid, long enemyId, int reason) {
        super(date, rid);
        this.enemyId = enemyId;
        this.reason = reason;
    }

    @Override
    public long getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(long enemyId) {
        this.enemyId = enemyId;
    }

    @Override
    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EnemyDestroyed that = (EnemyDestroyed) o;
        return enemyId == that.enemyId && reason == that.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enemyId, reason);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyDestroyed [");
        sb.append("enemyId=").append(enemyId);
        sb.append(", reason=").append(reason);
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
