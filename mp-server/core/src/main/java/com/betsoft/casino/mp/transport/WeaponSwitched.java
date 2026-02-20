package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

import java.util.List;

public class WeaponSwitched extends TObject {
    private int weaponId;
    private int seatId;
    private List<Weapon> weapons;

    public WeaponSwitched(long date, int rid, int weaponId, int seatId, List<Weapon> weapons) {
        super(date, rid);
        this.weaponId = weaponId;
        this.seatId = seatId;
        this.weapons = weapons;
    }

    public int getWeaponId() {
        return weaponId;
    }

    public int getSeatId() {
        return seatId;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeaponSwitched [");
        sb.append("weaponId=").append(weaponId);
        sb.append(", seatId=").append(seatId);
        sb.append(", weapons=").append(weapons);
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
