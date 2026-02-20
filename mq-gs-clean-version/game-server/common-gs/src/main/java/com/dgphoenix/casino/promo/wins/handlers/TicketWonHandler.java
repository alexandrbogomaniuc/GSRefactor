package com.dgphoenix.casino.promo.wins.handlers;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;

/**
 * User: flsh
 * Date: 10.12.16.
 */
class TicketWonHandler implements IPrizeWonHandler<TicketPrize> {
    @Override
    public void handle(PromoCampaignMember member, DesiredPrize desiredPrize, IPrizeWonHelper handler,
                       TicketPrize prize) throws CommonException {
        //nop
    }
}