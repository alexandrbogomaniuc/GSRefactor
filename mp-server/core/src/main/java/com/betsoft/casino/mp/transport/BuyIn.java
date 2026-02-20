package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class BuyIn extends TInboundObject {

    @Deprecated
    private int ammoAmount;

    public BuyIn(long date, int rid, int ammoAmount) {
        super(date, rid);
        this.ammoAmount = ammoAmount;
    }

    public int getAmmoAmount() {
        return ammoAmount;
    }

    public void setAmmoAmount(int ammoAmount) {
        this.ammoAmount = ammoAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BuyIn buyIn = (BuyIn) o;

        return ammoAmount == buyIn.ammoAmount;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + ammoAmount;
        return result;
    }

    @Override
    public String toString() {
        return "BuyIn[" +
                "ammoAmount=" + ammoAmount +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
