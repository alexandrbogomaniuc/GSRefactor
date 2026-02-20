package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

import java.util.Objects;

public class ReBuyResponse extends TObject {
    private long ammoAmount;
    private long balance;
    private int reBuyCount;

    public ReBuyResponse(long date, int rid, long ammoAmount, long balance, int reBuyCount) {
        super(date, rid);
        this.ammoAmount = ammoAmount;
        this.balance = balance;
        this.reBuyCount = reBuyCount;
    }

    public long getAmmoAmount() {
        return ammoAmount;
    }

    public long getBalance() {
        return balance;
    }

    public int getReBuyCount() {
        return reBuyCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReBuyResponse that = (ReBuyResponse) o;
        return ammoAmount == that.ammoAmount &&
                balance == that.balance &&
                reBuyCount == that.reBuyCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ammoAmount, balance, reBuyCount);
    }

    @Override
    public String toString() {
        return "ReBuyResponse{" +
                "ammoAmount=" + ammoAmount +
                ", balance=" + balance +
                ", reBuyCount=" + reBuyCount +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
