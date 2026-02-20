package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IWeaponSurplus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WeaponSurplus implements IWeaponSurplus, Serializable {
    private int id;
    private int shots;
    private long winBonus;

    public WeaponSurplus(int id, int shots, long winBonus) {
        this.id = id;
        this.shots = shots;
        this.winBonus = winBonus;
    }

    public static List<WeaponSurplus> convert(List<IWeaponSurplus> weaponSurpluses) {
        List<WeaponSurplus> result = new ArrayList<>();
        for (IWeaponSurplus surplus : weaponSurpluses) {
            if (surplus instanceof WeaponSurplus) {
                result.add((WeaponSurplus) surplus);
            } else {
                result.add(new WeaponSurplus(surplus.getId(), surplus.getShots(), surplus.getWinBonus()));
            }
        }
        return result;
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
    public long getWinBonus() {
        return winBonus;
    }

    @Override
    public void setWinBonus(long winBonus) {
        this.winBonus = winBonus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeaponSurplus that = (WeaponSurplus) o;
        return id == that.id &&
                shots == that.shots &&
                winBonus == that.winBonus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shots, winBonus);
    }

    @Override
    public String toString() {
        return "WeaponSurplus[" +
                "id=" + id +
                ", shots=" + shots +
                ", winBonus=" + winBonus +
                ']';
    }
}
