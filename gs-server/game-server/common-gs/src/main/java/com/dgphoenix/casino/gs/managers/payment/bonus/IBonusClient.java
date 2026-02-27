package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAccountInfoResult;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAuthResult;

/**
 * User: ktd
 * Date: 29.03.11
 */

public interface IBonusClient {

    void bonusRelease(Bonus bonus, String extUserId) throws CommonException;

    BonusAuthResult authenticate(String token) throws CommonException;

    BonusAuthResult authenticate(String token, String gameId) throws CommonException;

    BonusAccountInfoResult getAccountInfo(String userId) throws CommonException;

}