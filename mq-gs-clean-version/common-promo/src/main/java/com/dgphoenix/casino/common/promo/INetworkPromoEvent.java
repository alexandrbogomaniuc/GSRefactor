package com.dgphoenix.casino.common.promo;

/**
 * User: flsh
 * Date: 4.12.2020.
 */
public interface INetworkPromoEvent {
    long getParentPromoCampaignId();

    INetworkPromoEventTemplate getNetworkPromoEventTemplate();
}
