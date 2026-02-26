package com.abs.casino.entities.game.requests;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.forms.game.ct.CommonCTStartGameForm;
import com.abs.casino.helpers.game.processors.CTStartGameProcessor;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public class CTStartGameRequest extends StartGameRequest<CommonCTStartGameForm> {
    Long balance;
    Long gameSessionId;

    public CTStartGameRequest(SessionInfo sessionInfo, AccountInfo accountInfo, CommonCTStartGameForm form,
                              boolean checkWalletOps) {
        super(sessionInfo, accountInfo, form, checkWalletOps);

        this.balance = form.getBalance();
        this.startGameProcessor = CTStartGameProcessor.getInstance();
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }
}
