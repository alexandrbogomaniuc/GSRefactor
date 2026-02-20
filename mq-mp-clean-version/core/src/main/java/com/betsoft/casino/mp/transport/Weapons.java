package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ITransportWeapon;
import com.betsoft.casino.mp.model.IWeapons;
import com.betsoft.casino.utils.TObject;

import java.util.ArrayList;
import java.util.List;

public class Weapons extends TObject implements IWeapons<Weapon> {
    private int ammoAmount;
    private boolean freeShots;
    private List<Weapon> weapons;

    public Weapons(long date, int rid, int ammoAmount, boolean freeShots, List<ITransportWeapon> weapons) {
        super(date, rid);
        this.ammoAmount = ammoAmount;
        this.freeShots = freeShots;
        this.weapons = weapons == null ? new ArrayList<>() : Weapon.convert(weapons);
    }

    @Override
    public int getAmmoAmount() {
        return ammoAmount;
    }

    @Override
    public boolean isFreeShots() {
        return freeShots;
    }

    @Override
    public List<Weapon> getWeapons() {
        return weapons;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Weapons [");
        sb.append("ammoAmount=").append(ammoAmount);
        sb.append(", freeShots=").append(freeShots);
        sb.append(", weapons=").append(weapons);
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
