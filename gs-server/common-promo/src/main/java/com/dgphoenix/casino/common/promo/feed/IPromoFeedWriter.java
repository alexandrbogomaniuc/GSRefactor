package com.abs.casino.common.promo.feed;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.IPromoCampaign;

/**
 * Created by vladislav on 12/15/16.
 */
public interface IPromoFeedWriter {
    boolean isReadyToWrite(IPromoCampaign promoCampaign);

    void write(IPromoCampaign promoCampaign) throws CommonException;
}
