package com.abs.casino.gs.managers.payment.bonus.restriction;

import com.dgphoenix.casino.common.cache.data.account.IAccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BaseBonus;
import com.abs.casino.common.cache.data.bonus.restriction.MassAwardRestriction;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerBalanceRestriction implements MassAwardRestriction {

    private static final Logger LOG = LogManager.getLogger(PlayerBalanceRestriction.class);
    private static final int VERSION = 0;
    private long massAwardId;
    private long minimumBalance;

    public PlayerBalanceRestriction(long massAwardId, long minimumBalance) {
        this.massAwardId = massAwardId;
        this.minimumBalance = minimumBalance;
    }

    public long getMinimumBalance() {
        return minimumBalance;
    }

    @Override
    public long getId() {
        return massAwardId;
    }

    @Override
    public boolean isValid(IAccountInfo accountInfo, BaseBonus bonus, ICurrency currency) {
        try {
            CurrencyRatesManager currencyRatesManager = ApplicationContextHelper.getApplicationContext()
                    .getBean("currencyRatesManager", CurrencyRatesManager.class);
            double playerBalance = currencyRatesManager.convert((accountInfo.getBalance()), accountInfo.getCurrency().getCode(), currency.getCode());
            LOG.debug("Player balance = " + playerBalance + " ; Minimum balance = " + minimumBalance);
            if (playerBalance >= minimumBalance) {
                LOG.debug("Bonus " + bonus.getId() + " available for account " + accountInfo.getId());
                return true;
            } else {
                LOG.debug("Bonus " + bonus.getId() + " unavailable for account " + accountInfo.getId());
                return false;
            }
        } catch (CommonException e) {
            LOG.error("Cannot validate player balance restriction : " + accountInfo.getId(), e);
            return false;
        }
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(massAwardId, true);
        output.writeLong(minimumBalance, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        massAwardId = input.readLong(true);
        minimumBalance = input.readLong(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PlayerBalanceRestriction");
        sb.append("[massAwardId=").append(massAwardId);
        sb.append(", minimumBalance=").append(minimumBalance);
        sb.append(']');
        return sb.toString();
    }

}
