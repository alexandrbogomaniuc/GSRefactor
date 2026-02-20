package com.dgphoenix.casino.common.cache.data.payment.transfer.processor;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 11.08.2009
 */
public interface IPaymentProcessor {
    PaymentTransaction processDeposit(AccountInfo accountInfo, Long gameSessionId, Long gameId, long amount,
                                      String extTransactionId, boolean realMoney, ClientType clientType,
                                      String comment) throws CommonException;

    PaymentTransaction processWithdrawal(AccountInfo accountInfo, Long gameSessionId, Long gameId, long amount,
                                         String extTransactionId, boolean realMoney, ClientType clientType,
                                         String comment) throws CommonException;

    PaymentTransaction processAdjustment(AccountInfo accountInfo, long transactionId) throws CommonException;

    void processRevokeDeposit(PaymentTransaction transaction, AccountInfo accountInfo) throws CommonException;

    void processRevokeWithdrawal(PaymentTransaction transaction, AccountInfo accountInfo) throws CommonException;

    boolean isTransferAfterGameClosed();

    boolean isTrackable();
}
