package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IShot;
import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class Shot extends TInboundObject implements IShot {
    private int weaponId;
    private long enemyId;
    private float x;
    private float y;
    private boolean isPaidSpecialShot;
    private String bulletId;
    private int weaponPrice;

    public Shot(long date, int rid, int weaponId, long enemyId, float x, float y, boolean isPaidSpecialShot,
                String bulletId, int weaponPrice) {
        super(date, rid);
        this.weaponId = weaponId;
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.isPaidSpecialShot = isPaidSpecialShot;
        this.bulletId = bulletId;
        this.weaponPrice = weaponPrice;
    }

    public Shot(long date, int rid, int weaponId, long enemyId, float x, float y, boolean isPaidSpecialShot,
                String bulletId) {
        super(date, rid);
        this.weaponId = weaponId;
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.isPaidSpecialShot = isPaidSpecialShot;
        this.bulletId = bulletId;
    }

    @Override
    public int getFrequencyLimit() {
        return 150;
    }

    @Override
    public int getWeaponId() {
        return weaponId;
    }


    public void setWeaponId(int weaponId) {
        this.weaponId = weaponId;
    }

    @Override
    public int getRealWeaponId() {
        return weaponId;
    }

    @Override
    public String getBulletId() {
        return bulletId == null ? "" : bulletId;
    }

    public void setBulletId(String bulletId) {
        this.bulletId = bulletId;
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

    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
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
        return weaponPrice;
    }

    public void setWeaponPrice(int weaponPrice) {
        this.weaponPrice = weaponPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Shot shot = (Shot) o;
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
                ", bulletId=" + bulletId +
                ", weaponPrice=" + weaponPrice +
                ']';
    }
}
