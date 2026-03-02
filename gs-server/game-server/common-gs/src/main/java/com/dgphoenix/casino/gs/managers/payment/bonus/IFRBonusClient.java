package com.abs.casino.gs.managers.payment.bonus;

import com.abs.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.abs.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAccountInfoResult;
import com.abs.casino.gs.managers.payment.bonus.client.frb.FRBonusWinResult;

public interface IFRBonusClient {

    FRBonusWinResult bonusWin(long accountId, String extUserId, Boolean isRoundFinished, long bonusId, String extBonusId, long amount,
                              FRBWinOperation operation, long gameId, String extGameId, FRBonusWin frbonusWin) throws CommonException;

    BonusAccountInfoResult getAccountInfo(String userId) throws CommonException;


}
