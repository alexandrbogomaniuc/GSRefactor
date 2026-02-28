package com.abs.casino.gs.managers.payment.wallet.v3;

import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletStatusResult;

import java.util.Map;

public interface ICommonWalletClient extends com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient {

    public CommonWalletAuthResult auth(String token, ClientType clType) throws CommonException;

    public CommonWalletAuthResult auth(String token, String gameId, ClientType clType) throws CommonException;

    public CommonWalletAuthResult auth(String token, String gameId, String serverId, ClientType clType)
            throws CommonException;

    public CommonWalletAuthResult auth(String token, String gameId, String serverId, ClientType clType,
                                       Map<String, String> additionalParams)
            throws CommonException;

    public CommonWalletStatusResult getExternalTransactionStatus(long accountId, String extUserId,
                                                                 long transactionId,
                                                                 long bankId,
                                                                 CommonWalletOperation operation)
            throws CommonException;

    public void cancelTransaction(long accountId, String extUserId, long transactionId, long bankId)
            throws CommonException;

}