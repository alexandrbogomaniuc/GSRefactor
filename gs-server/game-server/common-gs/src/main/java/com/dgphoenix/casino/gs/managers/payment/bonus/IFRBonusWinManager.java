package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.FRBException;
import com.abs.casino.gs.managers.dblink.FRBonusDBLink;

public interface IFRBonusWinManager {

    void handleCreateFRBonusWin(AccountInfo accountInfo, long gameSessionId, long gameId) throws FRBException;

    void handleDestroyFRBonusWin(AccountInfo account, Long gameId) throws FRBException;

    void handleFailure(AccountInfo accountInfo) throws FRBException;

    void handleMPGameCredit(AccountInfo accountInfo, boolean isRoundFinished, long gameId,
                            long gameSessionId, long bonusId, SessionInfo sessionInfo, long winAmount) throws FRBException;

    void handleCredit(long accountId, boolean isRoundFinished, FRBonusDBLink dbLink) throws FRBException;

    void handleCreditCompleted(long accountId, boolean isRoundFinished, FRBonusDBLink dbLink) throws FRBException;

    void handleDebitCompleted(long accountId, FRBonusDBLink dbLink) throws FRBException;

    void handleFRBonusChangeStatus(AccountInfo account, long bonusId, BonusStatus status) throws FRBException;

    boolean isSendSingleFRBWin();
}
