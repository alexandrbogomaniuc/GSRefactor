package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IShot;
import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

public class StubShot extends TInboundObject implements IShot {
    private int weaponId;
    private long enemyId;
    private float x;
    private float y;
    private boolean isPaidSpecialShot;

    public StubShot(long date, int rid, int weaponId, long enemyId, float x, float y, boolean isPaidSpecialShot) {
        super(date, rid);
        this.weaponId = weaponId;
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.isPaidSpecialShot = isPaidSpecialShot;
    }

    @Override
    public int getFrequencyLimit() {
        return 150;
    }

    @Override
    public int getWeaponId() {
        return weaponId;
    }

    @Override
    public int getRealWeaponId() {
        return weaponId;
    }

    @Override
    public String getBulletId() {
        return "";
    }

    @Override
    public long getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(long enemyId) {
        this.enemyId = enemyId;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public boolean isPaidSpecialShot() {
        return isPaidSpecialShot;
    }

    public void setPaidSpecialShot(boolean paidSpecialShot) {
        isPaidSpecialShot = paidSpecialShot;
    }

    @Override
    public int getWeaponPrice() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StubShot shot = (StubShot) o;
        return weaponId == shot.weaponId &&
                enemyId == shot.enemyId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), weaponId, enemyId);
    }

    @Override
    public String toString() {
        return "Shot[" +
                "weaponId=" + weaponId +
                ", enemyId=" + enemyId +
                ", x=" + x +
                ", y=" + y +
                ", rid=" + rid +
                ", date=" + date +
                ", isPaidSpecialShot=" + isPaidSpecialShot +
                ']';
    }
}

