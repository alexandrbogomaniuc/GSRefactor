package com.dgphoenix.casino.common.cache.data.bonus.restriction;

import com.dgphoenix.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.cache.data.account.IAccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BaseBonus;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.esotericsoftware.kryo.KryoSerializable;

public interface MassAwardRestriction extends KryoSerializable, Identifiable {

    boolean isValid(IAccountInfo accountInfo, BaseBonus bonus, ICurrency currency);
}
