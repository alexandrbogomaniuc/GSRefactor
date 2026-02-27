package com.abs.casino.promo;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.IPromoCampaign;

public interface PromoFeedWriter {
    void write(IPromoCampaign promoCampaign) throws CommonException;
}
