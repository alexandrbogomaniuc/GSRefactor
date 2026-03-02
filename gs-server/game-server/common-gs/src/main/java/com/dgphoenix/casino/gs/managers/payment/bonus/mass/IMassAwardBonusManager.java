package com.abs.casino.gs.managers.payment.bonus.mass;

import com.abs.casino.common.cache.data.bonus.*;
import com.abs.casino.common.exception.CommonException;

import java.util.List;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 29.06.2021
 */
public interface IMassAwardBonusManager {

    void changeBonusStatus(long massAwardId, BonusStatus status);

    @SuppressWarnings("rawtypes")
    BaseMassAward get(long massAwardId);

    void createdFRBMassAward(long id, MassAwardType type, long bankId, List<Long> gameIds, long rounds, String comment,
                             String description, long startDate, long expirationDate, Long freeRoundValidity,
                             List<Long> accountIds, String countryCode, Long registeredFrom, Long frbTableRoundChips,
                             Long minimumBalance, BonusGameMode bonusGameMode, Long delayedMassAwardFrbId,
                             Long maxWinLimit) throws CommonException;

    void createdBonusMassAward(long id, MassAwardType type, long bankId, List<Long> gameIds,
                               String comment, String description, long startDate, long expirationDate,
                               List<Long> accountIds, String countryCode, Long registeredFrom,
                               BonusType bonusType, long amount, double rolloverMultiplier,
                               BonusGameMode bonusGameMode, long balance, boolean autoRelease,
                               Long delayedMassAwardId, Double maxWinMultiplier) throws CommonException;

    Long getMassAwardIdByDelayedMassAwardId(long delayedMassAwardFrbId);
}
