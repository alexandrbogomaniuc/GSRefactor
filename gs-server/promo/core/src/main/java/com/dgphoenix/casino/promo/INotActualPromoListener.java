package com.abs.casino.promo;

import com.abs.casino.common.exception.CommonException;

public interface INotActualPromoListener {

    void notifyCampaignBecameNotActual(long campaignId) throws CommonException;
}
