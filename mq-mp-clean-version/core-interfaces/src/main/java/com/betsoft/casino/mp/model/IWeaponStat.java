package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 13.02.19.
 */
public interface IWeaponStat extends KryoSerializable {
    void updateData(Money payouts, Money payBets, boolean isKilled);

    int getCnt();

    Money getPayouts();

    int getCntHits();

    void setCnt(int cnt);

    void setPayouts(Money payouts);

    void setCntHits(int cntHits);

    public Money getPayBets();

    public void setPayBets(Money payBets);
}
