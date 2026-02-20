package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ITransportWeapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User: flsh
 * Date: 09.06.17.
 */
public class Weapon implements ITransportWeapon {
    private int id;
    private int shots;
    private int sourceId;

    public Weapon(int id, int shots) {
        this.id = id;
        this.shots = shots;
        this.sourceId = -1;
    }

    public Weapon(int id, int shots, int sourceId) {
        this.id = id;
        this.shots = shots;
        this.sourceId = sourceId;
    }

    public static List<Weapon> convert(List<ITransportWeapon> weapons) {
        ArrayList<Weapon> result = new ArrayList<>();
        for (ITransportWeapon weapon : weapons) {
            result.add(new Weapon(weapon.getId(), weapon.getShots()));
        }
        return result;
    }

    @Override
    public int getSourceId() {
        return sourceId;
    }

    @Override
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
        Weapon weapon = (Weapon) o;
        return id == weapon.id && shots == weapon.shots && sourceId == weapon.sourceId;
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
