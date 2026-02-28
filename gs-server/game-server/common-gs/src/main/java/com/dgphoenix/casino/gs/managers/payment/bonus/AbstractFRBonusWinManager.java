package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.abs.casino.common.exception.FRBException;
import com.abs.casino.common.util.ILongIdGenerator;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.managers.dblink.FRBonusDBLink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractFRBonusWinManager<T extends FRBonusWin, V extends FRBWinOperation>
        implements IFRBonusWinManager {
    private static final Logger LOG = LogManager.getLogger(AbstractFRBonusWinManager.class);
    private final long bankId;


    protected AbstractFRBonusWinManager(long bankId) {
        this.bankId = bankId;
    }

    public long getBankId() {
        return bankId;
    }

    protected long generateRoundId() {
        return getIdGenerator().getNext(FRBonusWin.class);
    }

    protected long generateOperationId() {
        return getIdGenerator().getNext(FRBWinOperation.class);
    }

    protected ILongIdGenerator getIdGenerator() {
        return GameServer.getInstance().getIdGenerator();
    }

    @Override
    public void handleFailure(AccountInfo accountInfo) throws FRBException {
        //must be externally cluster synchronized
        processHandleFailure(accountInfo);
    }

    protected abstract void processHandleFailure(AccountInfo accountInfo) throws FRBException;

    protected abstract void processRealDebitCompleted(FRBonusDBLink dbLink) throws FRBException;


    @Override
    public void handleDebitCompleted(long accountId, FRBonusDBLink dbLink) throws FRBException {
        if (dbLink.getMode() == GameMode.REAL) {
            processRealDebitCompleted(dbLink);
        }
    }

    @Override
    public void handleFRBonusChangeStatus(AccountInfo account, long bonusId, BonusStatus status) throws FRBException {
    }

    @Override
    public boolean isSendSingleFRBWin() {
        return false;
    }
}
