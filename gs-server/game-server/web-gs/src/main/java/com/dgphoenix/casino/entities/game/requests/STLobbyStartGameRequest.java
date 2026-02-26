package com.abs.casino.entities.game.requests;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.forms.game.cwv3.CWOnlyStartSTGameForm;
import com.abs.casino.helpers.game.processors.STLobbyStartGameProcessor;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public class STLobbyStartGameRequest<F extends CWOnlyStartSTGameForm> extends StartGameRequest<F> {
    private boolean updateBalance;

    public STLobbyStartGameRequest(SessionInfo sessionInfo, AccountInfo accountInfo, F form, boolean checkWalletOps) {
        super(sessionInfo, accountInfo, form, checkWalletOps);

        this.updateBalance = form.isUpdateBalance();
        this.startGameProcessor = STLobbyStartGameProcessor.getInstance();
    }

    public boolean isUpdateBalance() {
        return updateBalance;
    }

    public void setUpdateBalance(boolean updateBalance) {
        this.updateBalance = updateBalance;
    }
}
