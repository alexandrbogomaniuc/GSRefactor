package com.abs.casino.gs.managers.payment.transfer.processor;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.abs.casino.common.cache.data.payment.transfer.processor.IPaymentProcessor;
import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: plastical
 * Date: 07.06.2010
 */
public abstract class AbstractPaymentProcessor implements IPaymentProcessor {
    protected long bankId;

    public AbstractPaymentProcessor(long bankId) {
        this.bankId = bankId;
    }


    @Override
    public boolean isTransferAfterGameClosed() {
        return true;
    }

    @Override
    public PaymentTransaction processAdjustment(AccountInfo accountInfo, long transactionId) throws CommonException {
        throw new CommonException("Operation not supported");//override if need support
    }

    @Override
    public boolean isTrackable() {
        return true;
    }
}
