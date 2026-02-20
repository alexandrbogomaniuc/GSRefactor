package com.dgphoenix.casino.promo.wins.handlers;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 10.12.16.
 */
class InstantMoneyWonHandler implements IPrizeWonHandler<InstantMoneyPrize> {
    private static final Logger LOG = LogManager.getLogger(InstantMoneyWonHandler.class);

    @Override
    public void handle(PromoCampaignMember member, DesiredPrize desiredPrize, IPrizeWonHelper handler,
                       InstantMoneyPrize prize)
            throws CommonException {
        //transaction must be started
        handler.changePlayerBalance(prize.getAmount());
        LOG.debug("handle: success={}", prize);
    }
}
