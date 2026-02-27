package com.abs.casino.promo.events.process;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 06.02.17.
 */
public class BonusEventQualifier implements ParticipantEventQualifier<PlayerBonusEvent> {
    private static final Logger LOG = LogManager.getLogger(BonusEventQualifier.class);

    @Override
    public SignificantEventType getEventType() {
        return SignificantEventType.BONUS;
    }

    @Override
    public boolean qualifyEvent(PlayerBonusEvent event, IPrize campaignPrize, DesiredPrize desiredPrize, PromoCampaignMember member, IPromoCampaign campaign,
                                boolean updateMember) throws CommonException {
        return campaignPrize.qualifyBonus(campaign, member, desiredPrize, event);
    }

}
