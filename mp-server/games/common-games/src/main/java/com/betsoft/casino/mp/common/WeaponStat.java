package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.IWeaponStat;
import com.betsoft.casino.mp.model.Money;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class WeaponStat implements IWeaponStat {
    private static final byte VERSION = 0;

    private int cnt;
    private Money payouts = Money.ZERO;
    private int cntHits = 0;
    private Money payBets = Money.ZERO;

    public WeaponStat() {

    }

    public WeaponStat(int cnt, Money payouts, int cntHits, Money payBets) {
        this.cnt = cnt;
        this.payouts = payouts;
        this.cntHits = cntHits;
        this.payBets = payBets;
    }

    @Override
    public void updateData(Money payouts, Money payBets, boolean isKilled) {
        this.payouts = this.payouts.add(payouts);
        this.payBets = this.payBets.add(payBets);
        this.cnt++;
        if (isKilled) {
            this.cntHits++;
        }
    }

    @Override
    public int getCnt() {
        return cnt;
    }

    @Override
    public Money getPayouts() {
        return payouts;
    }

    @Override
    public int getCntHits() {
        return cntHits;
    }

    @Override
    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    @Override
    public void setPayouts(Money payouts) {
        this.payouts = payouts;
    }

    @Override
    public void setCntHits(int cntHits) {
        this.cntHits = cntHits;
    }

    @Override
    public Money getPayBets() {
        return payBets;
    }

    @Override
    public void setPayBets(Money payBets) {
        this.payBets = payBets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeaponStat that = (WeaponStat) o;
        return cnt == that.cnt &&
                Objects.equals(payouts, that.payouts) && Objects.equals(payBets, that.payBets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cnt, payouts, payBets);
    }

    @Override
    public String toString() {
        return "WeaponStat[" +
                "cnt=" + cnt +
                ", payouts=" + payouts +
                ", cntHits=" + cntHits +
                ", payBets=" + payBets +
                ']';
    }


    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(cnt, true);
        output.writeInt(cntHits, true);
        kryo.writeObject(output, payouts);
        kryo.writeObject(output, payBets);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        cnt = input.readInt(true);
        cntHits = input.readInt(true);
        payouts = kryo.readObject(input, Money.class);
        payBets = kryo.readObject(input, Money.class);
    }
}
