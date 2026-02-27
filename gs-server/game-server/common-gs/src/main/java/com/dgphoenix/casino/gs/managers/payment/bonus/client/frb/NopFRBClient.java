package com.abs.casino.gs.managers.payment.bonus.client.frb;

import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.managers.payment.bonus.AbstractBonusClient;
import com.dgphoenix.casino.gs.managers.payment.bonus.IFRBonusClient;
import com.dgphoenix.casino.gs.managers.payment.bonus.client.BonusAccountInfoResult;

/**
 * User: Grien
 * Date: 04.06.2013 20:53
 */
public class NopFRBClient extends AbstractBonusClient implements IFRBonusClient {
    public NopFRBClient(long bankId) {
        super(bankId);
    }

    @Override
    public FRBonusWinResult bonusWin(long accountId, String extUserId, Boolean isRoundFinished, long bonusId, String extBonusId, long amount,
                                     FRBWinOperation operation, long gameId, String extGameId, FRBonusWin frbonusWin)
            throws CommonException {
        throw new CommonException("Not supported method");
    }

    @Override
    public BonusAccountInfoResult getAccountInfo(String userId) throws CommonException {
        throw new CommonException("Not supported method");
    }
}
