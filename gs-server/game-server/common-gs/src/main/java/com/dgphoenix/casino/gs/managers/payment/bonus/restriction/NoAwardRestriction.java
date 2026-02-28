package com.abs.casino.gs.managers.payment.bonus.restriction;

import com.abs.casino.common.cache.data.account.IAccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BaseBonus;
import com.abs.casino.common.cache.data.bonus.restriction.MassAwardRestriction;
import com.abs.casino.common.cache.data.currency.ICurrency;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class NoAwardRestriction implements MassAwardRestriction {

    @Override
    public boolean isValid(IAccountInfo accountInfo, BaseBonus bonus, ICurrency currency) {
        return true;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void write(Kryo kryo, Output output) {

    }

    @Override
    public void read(Kryo kryo, Input input) {

    }
}
