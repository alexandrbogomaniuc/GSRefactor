package com.abs.casino.promo.events.process;

import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class EndRoundEventQualifier implements ParticipantEventQualifier<EndRoundEvent> {
    private static final Logger LOG = LogManager.getLogger(EndRoundEventQualifier.class);

    private ICurrencyRateManager currencyRateManager;

    @Override
    public SignificantEventType getEventType() {
        return SignificantEventType.END_ROUND;
    }

    public EndRoundEventQualifier(ICurrencyRateManager currencyRateManager) {
        this.currencyRateManager = currencyRateManager;
    }

    @Override
    public boolean qualifyEvent(EndRoundEvent event, IPrize campaignPrize, DesiredPrize desiredPrize, PromoCampaignMember member, IPromoCampaign campaign,
                                boolean updateMember) throws CommonException {
        TournamentMemberRank memberRank = event.getTournamentMemberRank();
        if (memberRank != null && !memberRank.getRoundStats().isEmpty()) {
            memberRank.removeRoundStat(event.getGameId());
        }
        return campaignPrize.qualifyEndRound(campaign, member, desiredPrize, event, currencyRateManager, campaign.getBaseCurrency());
    }
}
