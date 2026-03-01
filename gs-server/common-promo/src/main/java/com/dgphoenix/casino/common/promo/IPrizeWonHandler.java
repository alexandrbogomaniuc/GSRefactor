package com.abs.casino.common.promo;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.DesiredPrize;
import com.abs.casino.common.promo.IPrize;
import com.abs.casino.common.promo.PromoCampaignMember;

/**
 * User: flsh
 * Date: 10.12.16.
 */
public interface IPrizeWonHandler<T extends IPrize> {
    void handle(PromoCampaignMember member, DesiredPrize desiredPrize, IPrizeWonHelper balanceChanger, T prize)
            throws CommonException;
}
