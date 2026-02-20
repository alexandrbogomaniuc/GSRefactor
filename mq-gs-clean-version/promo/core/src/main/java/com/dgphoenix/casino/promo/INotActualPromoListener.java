package com.dgphoenix.casino.promo;

import com.dgphoenix.casino.common.exception.CommonException;

public interface INotActualPromoListener {

    void notifyCampaignBecameNotActual(long campaignId) throws CommonException;
}
