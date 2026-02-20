package com.betsoft.casino.mp.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.Objects;

public class SeatBullet implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private long bulletTime;
    private float bulletAngle;
    private String bulletId;
    private int startPointX;
    private int startPointY;
    private int endPointX;
    private int endPointY;
    private int weaponId;

    public SeatBullet() {}

    public SeatBullet(long bulletTime, float bulletAngle, String bulletId, int startPointX,
                      int startPointY, int endPointX, int endPointY, int weaponId) {
        this.bulletTime = bulletTime;
        this.bulletAngle = bulletAngle;
        this.bulletId = bulletId;
        this.startPointX = startPointX;
        this.startPointY = startPointY;
        this.endPointX = endPointX;
        this.endPointY = endPointY;
        this.weaponId = weaponId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(bulletTime);
        output.writeFloat(bulletAngle);
        output.writeString(bulletId);
        output.writeInt(startPointX);
        output.writeInt(startPointY);
        output.writeInt(endPointX);
        output.writeInt(endPointY);
        output.writeInt(weaponId);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        bulletTime = input.readLong();
        bulletAngle = input.readFloat();
        bulletId = input.readString();
        startPointX = input.readInt();
        startPointY = input.readInt();
        endPointX = input.readInt();
        endPointY = input.readInt();
        weaponId = input.readInt();
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
        SeatBullet that = (SeatBullet) o;
        return bulletTime == that.bulletTime && Float.compare(that.bulletAngle, bulletAngle) == 0
                && startPointX == that.startPointX && startPointY == that.startPointY && endPointX == that.endPointX
                && endPointY == that.endPointY && Objects.equals(bulletId, that.bulletId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bulletTime, bulletAngle, bulletId, startPointX, startPointY, endPointX, endPointY);
    }

    @Override
    public String toString() {
        return "SeatBullet[" +
                "bulletTime=" + bulletTime +
                ", bulletAngle=" + bulletAngle +
                ", bulletId=" + bulletId +
                ", startPointX=" + startPointX +
                ", startPointY=" + startPointY +
                ", endPointX=" + endPointX +
                ", endPointY=" + endPointY +
                ", weaponId=" + weaponId +
                ']';
    }
}
