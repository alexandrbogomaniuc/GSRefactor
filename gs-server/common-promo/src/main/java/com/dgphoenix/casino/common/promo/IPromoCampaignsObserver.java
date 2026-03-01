package com.abs.casino.common.promo;

import com.abs.casino.common.promo.Status;

/**
 * Created by vladislav on 3/24/17.
 */
public interface IPromoCampaignsObserver {
    void notifyCampaignCreated(long campaignId);

    void notifyCampaignChanged(long campaignId);

    void notifyCampaignStatusChanged(long campaignId, Status oldStatus, Status newStatus);
}
