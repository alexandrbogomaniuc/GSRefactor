package com.abs.casino.sm;

import com.abs.casino.actions.enter.game.cwv3.CWStartGameForm;
import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.sm.CWPlayerSessionManager;
import com.abs.casino.sm.login.GameLoginRequest;

public class CWv3PlayerSessionManager extends CWPlayerSessionManager<GameLoginRequest, CWStartGameForm> {

    public CWv3PlayerSessionManager(long bankId) {
        super(bankId);
    }

    @Override
    protected void addSessionParameters(AccountInfo accountInfo, SessionInfo sessionInfo, CWStartGameForm form)
            throws CommonException {
        try {
            if (!form.getGameMode().equals(GameMode.FREE) && !SessionHelper.getInstance().getTransactionData().isAppliedAutoFinishLogic()) {
                accountInfo.setBalance(form.getBalance());
            }
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }
}
