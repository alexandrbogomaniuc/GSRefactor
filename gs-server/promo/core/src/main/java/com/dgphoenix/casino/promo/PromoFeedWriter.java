package com.abs.casino.promo;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.promo.IPromoCampaign;

public interface PromoFeedWriter {
    void write(IPromoCampaign promoCampaign) throws CommonException;
}
