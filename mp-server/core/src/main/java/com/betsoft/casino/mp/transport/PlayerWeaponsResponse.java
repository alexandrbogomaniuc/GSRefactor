package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

import java.util.List;
import java.util.Map;

public class PlayerWeaponsResponse extends TObject {
    private Map<Float, List<Weapon>> weapons;

    public PlayerWeaponsResponse(long date, int rid, Map<Float, List<Weapon>> weapons) {
        super(date, rid);
        this.weapons = weapons;
    }

    public Map<Float, List<Weapon>> getWeapons() {
        return weapons;
    }

    public void setWeapons(Map<Float, List<Weapon>> weapons) {
        this.weapons = weapons;
    }
}
