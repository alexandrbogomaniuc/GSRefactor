package com.abs.casino.common.promo;

import com.abs.casino.common.promo.INetworkPromoEventTemplate;
/**
 * User: flsh
 * Date: 4.12.2020.
 */
public interface INetworkPromoEvent {
    long getParentPromoCampaignId();

    INetworkPromoEventTemplate getNetworkPromoEventTemplate();
}
