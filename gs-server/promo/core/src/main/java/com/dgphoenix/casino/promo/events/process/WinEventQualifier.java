package com.abs.casino.promo.events.process;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 06.02.17.
 */
public class WinEventQualifier implements ParticipantEventQualifier<PlayerWinEvent> {
    private static final Logger LOG = LogManager.getLogger(WinEventQualifier.class);

    private final ICurrencyRateManager currencyRateManager;

    public WinEventQualifier(ICurrencyRateManager currencyRateManager) {
        this.currencyRateManager = currencyRateManager;
    }

    @Override
    public SignificantEventType getEventType() {
        return SignificantEventType.WIN;
    }

    @Override
    public boolean qualifyEvent(PlayerWinEvent event, IPrize campaignPrize, DesiredPrize desiredPrize, PromoCampaignMember member, IPromoCampaign campaign,
                                boolean updateMember) throws CommonException {
        if (updateMember && event.getWinAmount() != null) {
            member.updateWins(event.getWinAmount());
        }
        return campaignPrize.qualifyWin(campaign, member, desiredPrize, event, currencyRateManager, campaign.getBaseCurrency());
    }

}
