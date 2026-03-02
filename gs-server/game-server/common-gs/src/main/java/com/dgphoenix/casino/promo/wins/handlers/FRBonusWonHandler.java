package com.abs.casino.promo.wins.handlers;

import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bonus.FRBonus;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.*;
import com.abs.casino.gs.managers.payment.bonus.FRBonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 10.12.16.
 */
class FRBonusWonHandler implements IPrizeWonHandler<FRBonusPrize> {
    private static final Logger LOG = LogManager.getLogger(FRBonusWonHandler.class);

    @Override
    public void handle(PromoCampaignMember member, DesiredPrize desiredPrize, IPrizeWonHelper helper, FRBonusPrize prize)
            throws CommonException {
        //transaction must be started
        AccountInfo account = SessionHelper.getInstance().getTransactionData().getAccount();
        FRBonus frBonus = FRBonusManager.getInstance().awardBonus(account, prize.getRounds(),
                "promo_" + member.getCampaignId() + "_" + account.getId() + "_" + System.currentTimeMillis(),
                prize.getGameIds(), "Promo prize", "Promo: " + member.getCampaignId(), System.currentTimeMillis(), true,
                prize.getStartDate() == null ? System.currentTimeMillis() : prize.getStartDate(),
                prize.getExpirationDate(), prize.getFreeRoundValidity(), prize.getFrbTableRoundChips(), null /* coin value by profile */, null);
        LOG.debug("handle: success={}", frBonus);
    }
}
