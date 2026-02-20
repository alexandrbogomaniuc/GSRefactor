package com.dgphoenix.casino.common.cache.data.payment.transfer.paymentmean;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;

/**
 * User: flsh
 * Date: 11.08.2009
 */
public class CereusPaymentMean extends AbstractPaymentMean {
    private PaymentMeanId paymentMeanId;
    private boolean active;

    public CereusPaymentMean(AccountInfo accountInfo) {
        super(accountInfo.getId());
        paymentMeanId = PaymentMeanId.FOUNDS_ON_DEMAND;
        active = !accountInfo.isLocked();
    }

    public PaymentMeanType getPaymentMeanType() {
        return PaymentMeanType.ACCOUNT_INFO_EXTERNAL_ID;
    }

    public PaymentMeanId getPaymentMeanId() {
        return paymentMeanId;
    }

    public boolean isActive() {
        return active;
    }
}
