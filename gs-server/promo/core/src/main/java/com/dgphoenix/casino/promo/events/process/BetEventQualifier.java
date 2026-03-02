package com.abs.casino.promo.events.process;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by vladislav on 12/27/16.
 */
public class BetEventQualifier implements ParticipantEventQualifier<PlayerBetEvent> {
    private static final Logger LOG = LogManager.getLogger(BetEventQualifier.class);

    private final ICurrencyRateManager currencyRateManager;

    public BetEventQualifier(ICurrencyRateManager currencyRateManager) {
        this.currencyRateManager = currencyRateManager;
    }

    @Override
    public SignificantEventType getEventType() {
        return SignificantEventType.BET;
    }

    @Override
    public boolean qualifyEvent(PlayerBetEvent event, IPrize campaignPrize, DesiredPrize desiredPrize, PromoCampaignMember member, IPromoCampaign campaign,
                                boolean updateMember) throws CommonException {
        if (updateMember) {
            campaign.getTemplate().updateMemberBetInfo(member, event, campaign, currencyRateManager);
        }
        return campaignPrize.qualifyBet(member, desiredPrize, event, currencyRateManager, campaign.getBaseCurrency());
    }


}
