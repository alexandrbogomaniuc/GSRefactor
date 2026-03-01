package com.abs.casino.common.promo;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.DesiredPrize;
import com.dgphoenix.casino.common.promo.IPrize;
import com.dgphoenix.casino.common.promo.PromoCampaignMember;

/**
 * User: flsh
 * Date: 10.12.16.
 */
public interface IPrizeWonHandler<T extends IPrize> {
    void handle(PromoCampaignMember member, DesiredPrize desiredPrize, IPrizeWonHelper balanceChanger, T prize)
            throws CommonException;
}
