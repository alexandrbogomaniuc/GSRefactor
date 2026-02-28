package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BaseBonus;
import com.abs.casino.common.exception.BonusException;

import java.util.List;

public interface AbstractBonusManager<T extends BaseBonus> {

    abstract T getById(long id);

    abstract boolean cancelBonus(T bonus) throws BonusException;

    abstract T get(long bankId, String extId) throws BonusException;

    abstract List<T> getFinishedBonuses(AccountInfo accountInfo) throws BonusException;

    abstract List<T> getActiveBonuses(AccountInfo accountInfo);

    boolean expireBonus(T bonus) throws BonusException;

    void invalidateClient(long bankId);
}
