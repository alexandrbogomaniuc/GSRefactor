package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 11.06.17.
 */
public class BuyInResponse extends TObject {
    private long ammoAmount;
    private long balance;

    public BuyInResponse(long date, int rid, long ammoAmount, long balance) {
        super(date, rid);
        this.ammoAmount = ammoAmount;
        this.balance = balance;
    }

    public long getAmmoAmount() {
        return ammoAmount;
    }

    public void setAmmoAmount(long ammoAmount) {
        this.ammoAmount = ammoAmount;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BuyInResponse that = (BuyInResponse) o;

        if (ammoAmount != that.ammoAmount) return false;
        return balance == that.balance;

    }

    @Override
    public String toString() {
        return "BuyInResponse[" +
                "ammoAmount=" + ammoAmount +
                ", balance=" + balance +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
