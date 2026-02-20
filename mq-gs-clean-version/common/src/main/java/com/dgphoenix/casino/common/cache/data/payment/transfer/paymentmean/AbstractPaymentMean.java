package com.dgphoenix.casino.common.cache.data.payment.transfer.paymentmean;

/**
 * User: flsh
 * Date: 11.08.2009
 */
public abstract class AbstractPaymentMean implements IPaymentMean {
    private Long userId;

    protected AbstractPaymentMean(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
