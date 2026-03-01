package com.abs.casino.gs.managers.payment.wallet.v4;

import com.abs.casino.common.client.canex.request.privateroom.Status;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletOperation;

/**
 * User: flsh
 * Date: 9/27/12
 */
public interface ICommonWalletClient extends
        com.abs.casino.gs.managers.payment.wallet.v3.ICommonWalletClient {
    boolean refundBet(long operationStartTime, long accountId, String extUserId,
                      CommonWalletOperation debitOperation, long gameId) throws CommonException;

    boolean updatePlayerStatusInPrivateRoom(String privateRoomId, String nickname, String externalId, Status status, int bankId) throws CommonException;
}
