package com.dgphoenix.casino.common.promo.messages.client.requests;

import java.util.Arrays;

/**
 * Created by vladislav on 12/1/16.
 */
public class PromoEnter extends ClientRequest {
    private long[] promoIds;

    public PromoEnter() {
    }

    public PromoEnter(long id, long[] promoIds) {
        super(id);
        this.promoIds = promoIds;
    }

    public long[] getPromoIds() {
        return promoIds;
    }

    public void setPromoIds(long[] promoIds) {
        this.promoIds = promoIds;
    }

    @Override
    public String toString() {
        return "PromoEnter[" +
                super.toString() +
                ", promoIds=" + Arrays.toString(promoIds) +
                ']';
    }
}
