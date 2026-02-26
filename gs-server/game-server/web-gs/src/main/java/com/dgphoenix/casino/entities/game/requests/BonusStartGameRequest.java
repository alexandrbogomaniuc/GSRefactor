package com.abs.casino.entities.game.requests;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.forms.game.CommonBonusStartGameForm;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public class BonusStartGameRequest<F extends CommonBonusStartGameForm> extends StartGameRequest<F> {
    Long bonusId;

    public BonusStartGameRequest(SessionInfo sessionInfo, AccountInfo accountInfo, F form, boolean checkWalletOps) {
        super(sessionInfo, accountInfo, form, checkWalletOps);

        this.bonusId = form.getBonusId();
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }
}
