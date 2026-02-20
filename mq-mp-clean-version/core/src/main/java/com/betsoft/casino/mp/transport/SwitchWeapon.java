package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class SwitchWeapon extends TInboundObject {
    private int weaponId;

    public SwitchWeapon(long date, int rid, int weaponId) {
        super(date, rid);
        this.weaponId = weaponId;
    }

    public int getWeaponId() {
        return weaponId;
    }

    @Override
    public String toString() {
        return "SwitchWeapon[" +
                "weaponId=" + weaponId +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
