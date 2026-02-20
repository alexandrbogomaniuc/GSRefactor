package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class PurchaseWeaponLootBox extends TInboundObject {
    private int box;

    public PurchaseWeaponLootBox(long date, int rid, int box) {
        super(date, rid);
        this.box = box;
    }

    public int getBox() {
        return box;
    }

    @Override
    public String toString() {
        return "PurchaseWeaponLootBox[" +
                "box=" + box +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
