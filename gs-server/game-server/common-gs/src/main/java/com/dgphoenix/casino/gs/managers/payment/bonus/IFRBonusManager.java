package com.abs.casino.gs.managers.payment.bonus;


import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bonus.BaseMassAward;
import com.abs.casino.common.cache.data.bonus.FRBonus;
import com.abs.casino.common.exception.BonusException;

import java.util.List;
import java.util.function.Supplier;

public interface IFRBonusManager extends AbstractBonusManager<FRBonus> {

    /**
     * @param externalBonusIdComposer provides external id which will be used to get this bonus
     */
    FRBonus awardBonus(AccountInfo accountInfo,
                       long rounds,
                       String extId,
                       List<Long> gameIds,
                       String description,
                       String comment,
                       long timeAwarded,
                       boolean internal,
                       Long startDate,
                       Long expirationDate,
                       Long freeRoundValidity,
                       Long frbTableRoundChips,
                       Long coinValue,
                       Long maxWinLimit,
                       Supplier<String> externalBonusIdComposer) throws BonusException;

    FRBonus awardBonus(AccountInfo accountInfo,
                       long rounds,
                       String extId,
                       List<Long> gameIds,
                       String description,
                       String comment,
                       long timeAwarded,
                       boolean internal,
                       Long startDate,
                       Long expirationDate,
                       Long freeRoundValidity,
                       Long frbTableRoundChips,
                       Long coinValue,
                       Long maxWinLimit) throws BonusException;

    FRBonus awardBonusOnMassAward(AccountInfo accountInfo,
                                  BaseMassAward massAward) throws BonusException;

    boolean closeBonus(FRBonus bonus) throws BonusException;

    IFRBonusClient getClient(long bankId) throws BonusException;

    void checkMassAwardsForAccount(AccountInfo accountInfo) throws BonusException;
}
