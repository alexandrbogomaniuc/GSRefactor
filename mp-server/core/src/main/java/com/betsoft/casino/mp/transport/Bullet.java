package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

public class Bullet extends TInboundObject {
    private long bulletTime;
    private float bulletAngle;
    private String bulletId;
    private int startPointX;
    private int startPointY;
    private int endPointX;
    private int endPointY;
    private int weaponId;

    public Bullet(long date, int rid, long bulletTime, float bulletAngle,
                  String bulletId, int startPointX, int startPointY, int endPointX, int endPointY, int weaponId) {
        super(date, rid);
        this.bulletTime = bulletTime;
        this.bulletAngle = bulletAngle;
        this.bulletId = bulletId;
        this.startPointX = startPointX;
        this.startPointY = startPointY;
        this.endPointX = endPointX;
        this.endPointY = endPointY;
        this.weaponId = weaponId;
    }

    public long getBulletTime() {
        return bulletTime;
    }

    public void setBulletTime(long bulletTime) {
        this.bulletTime = bulletTime;
    }

    public float getBulletAngle() {
        return bulletAngle;
    }

    public void setBulletAngle(float bulletAngle) {
        this.bulletAngle = bulletAngle;
    }

    public String getBulletId() {
        return bulletId;
    }

    public void setBulletId(String bulletId) {
        this.bulletId = bulletId;
    }

    public int getStartPointX() {
        return startPointX;
    }

    public void setStartPointX(int startPointX) {
        this.startPointX = startPointX;
    }

    public int getStartPointY() {
        return startPointY;
    }

    public void setStartPointY(int startPointY) {
        this.startPointY = startPointY;
    }

    public int getEndPointX() {
        return endPointX;
    }

    public void setEndPointX(int endPointX) {
        this.endPointX = endPointX;
    }

    public int getEndPointY() {
        return endPointY;
    }

    public void setEndPointY(int endPointY) {
        this.endPointY = endPointY;
    }

    public int getWeaponId() {
        return weaponId;
    }

    public void setWeaponId(int weaponId) {
        this.weaponId = weaponId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Bullet bullet = (Bullet) o;
        return bulletTime == bullet.bulletTime
                && Float.compare(bullet.bulletAngle, bulletAngle) == 0
                && bulletId.equals(bullet.bulletId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bulletTime, bulletAngle, bulletId);
    }

    @Override
    public int getFrequencyLimit() {
        return 150;
    }

    @Override
    public String toString() {
        return "Bullet[" +
                "bulletTime=" + bulletTime +
                ", bulletAngle=" + bulletAngle +
                ", bulletId=" + bulletId +
                ", startPointX=" + startPointX +
                ", startPointY=" + startPointY +
                ", endPointX=" + endPointX +
                ", endPointY=" + endPointY +
                ", weaponId=" + weaponId +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
