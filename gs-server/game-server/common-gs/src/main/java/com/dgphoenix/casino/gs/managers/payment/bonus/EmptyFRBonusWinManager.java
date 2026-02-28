package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.FRBException;
import com.abs.casino.gs.managers.dblink.FRBonusDBLink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by quant on 13.04.16.
 */
public class EmptyFRBonusWinManager extends OriginalFRBonusWinManager {
    private static final Logger LOG = LogManager.getLogger(EmptyFRBonusWinManager.class);

    public EmptyFRBonusWinManager(long bankId) throws CommonException {
        super(bankId);
    }

    @Override
    public void handleMPGameCredit(AccountInfo accountInfo, boolean isRoundFinished, long gameId, long gameSessionId,
                                   long bonusId, SessionInfo sessionInfo, long winAmount) throws FRBException {
        LOG.info("handleMPGameCredit accountId=" + accountInfo.getId() + ", bonusId=" + bonusId + ", winAmount: " + winAmount);
    }

    @Override
    public void handleCredit(long accountId, boolean isRoundFinished, FRBonusDBLink dbLink) throws FRBException {
        LOG.info("handleCredit accountId=" + accountId + ", bonusId=" + dbLink.getBonusId());
    }

    @Override
    public void handleCreditCompleted(long accountId, boolean isRoundFinished, FRBonusDBLink dbLink) throws FRBException {
        LOG.info("handleCreditCompleted accountId=" + accountId + ", isRoundFinished=" +
                isRoundFinished + ", bonusId=" + dbLink.getBonusId());
    }

    @Override
    public void handleDebitCompleted(long accountId, FRBonusDBLink dbLink) throws FRBException {
        LOG.info("handleDebitCompleted accountId=" + accountId + ", bonusId=" + dbLink.getBonusId());
    }
}
