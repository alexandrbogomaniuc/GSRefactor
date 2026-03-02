package com.abs.casino.promo.wins.handlers;

import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.cache.data.bonus.BonusGameMode;
import com.abs.casino.common.cache.data.bonus.BonusType;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.*;
import com.abs.casino.gs.managers.payment.bonus.BonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 10.12.16.
 */
class CacheBonusWonHandler implements IPrizeWonHandler<CacheBonusPrize> {
    private static final Logger LOG = LogManager.getLogger(CacheBonusWonHandler.class);

    @Override
    public void handle(PromoCampaignMember member, DesiredPrize desiredPrize, IPrizeWonHelper handler,
                       CacheBonusPrize prize)
            throws CommonException {
        //transaction must be started
        AccountInfo account = SessionHelper.getInstance().getTransactionData().getAccount();
        Bonus bonus = BonusManager.getInstance().awardBonus(account, BonusType.PROMO, prize.getAmount(),
                prize.getRolloverMultiplier(),
                "promo_" + member.getCampaignId() + "_" + account.getId() + "_" + System.currentTimeMillis(),
                prize.getGameIds(), BonusGameMode.ONLY, "Promo prize", "Promo: " + member.getCampaignId(),
                prize.getExpirationDate(), System.currentTimeMillis(), true, false, true,
                null, null);
        LOG.debug("handle: success={}", bonus);
    }
}
