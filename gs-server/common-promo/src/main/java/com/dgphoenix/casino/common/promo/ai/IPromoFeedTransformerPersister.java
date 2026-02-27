package com.abs.casino.common.promo.ai;

public interface IPromoFeedTransformerPersister {
    IPromoFeedTransformer getTransformer(long campaignId);
}
