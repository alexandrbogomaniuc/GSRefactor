package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.ITransportWeapon;

import java.util.Objects;

public class StubWeapon implements ITransportWeapon {
    private int id;
    private int shots;
    private int sourceId;

    public StubWeapon(int id, int shots) {
        this.id = id;
        this.shots = shots;
        this.sourceId = -1;
    }

    public StubWeapon(int id, int shots, int sourceId) {
        this.id = id;
        this.shots = shots;
        this.sourceId = sourceId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getShots() {
        return shots;
    }

    @Override
    public void setShots(int shots) {
        this.shots = shots;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubWeapon that = (StubWeapon) o;
        return id == that.id && shots == that.shots && sourceId == that.sourceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shots, sourceId);
    }

    @Override
    public String toString() {
        return "Weapon[" +
                "id=" + id +
                ", shots=" + shots +
                ", sourceId=" + sourceId +
                ']';
    }
}
