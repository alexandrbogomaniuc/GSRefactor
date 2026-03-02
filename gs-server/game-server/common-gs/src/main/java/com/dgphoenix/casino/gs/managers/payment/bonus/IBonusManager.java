package com.abs.casino.gs.managers.payment.bonus;

import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bonus.BaseMassAward;
import com.abs.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.cache.data.bonus.BonusGameMode;
import com.abs.casino.common.cache.data.bonus.BonusType;
import com.abs.casino.common.exception.BonusException;
import com.abs.casino.common.exception.CommonException;

import java.util.List;

public interface IBonusManager extends AbstractBonusManager<Bonus> {

    Bonus awardBonus(AccountInfo accountInfo,
                     BonusType type,
                     long amount,
                     double multiplier,
                     String extId,
                     List<Long> gameIds,
                     BonusGameMode bonusGameMode,
                     String description,
                     String comment,
                     long expirationDate,
                     long timeAwarded,
                     boolean internal,
                     boolean mass,
                     boolean autoRelease,
                     Long startDate,
                     Double maxWinMultiplier) throws BonusException;

    void releaseBonus(Bonus bonus) throws BonusException;

    boolean lostBonus(Bonus bonus) throws BonusException;

    IBonusClient getClient(long bankId) throws BonusException;

    void releaseBonusManually(long bonusId) throws CommonException;

    Bonus awardBonusOnMassAward(AccountInfo accountInfo, BaseMassAward massAward) throws BonusException;

    void checkMassAwardsForAccount(AccountInfo accountInfo) throws BonusException;

    boolean isBonusShouldBeLost(Bonus bonus, AccountInfo accountInfo);
}
