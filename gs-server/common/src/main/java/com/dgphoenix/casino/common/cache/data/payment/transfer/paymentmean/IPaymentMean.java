package com.dgphoenix.casino.common.cache.data.payment.transfer.paymentmean;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 11.08.2009
 */
public interface IPaymentMean extends Serializable {
    PaymentMeanType getPaymentMeanType();

    PaymentMeanId getPaymentMeanId();

    Long getUserId();

    boolean isActive();
}
