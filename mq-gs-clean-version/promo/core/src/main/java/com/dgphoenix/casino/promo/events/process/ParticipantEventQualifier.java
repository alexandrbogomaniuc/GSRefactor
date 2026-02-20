package com.dgphoenix.casino.promo.events.process;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;

/**
 * Created by vladislav on 12/27/16.
 */
public interface ParticipantEventQualifier<T extends IParticipantEvent> {

    SignificantEventType getEventType();

    boolean qualifyEvent(T event, IPrize campaignPrize, DesiredPrize desiredPrize, PromoCampaignMember member, IPromoCampaign campaign,
                         boolean updateMember) throws CommonException;
}