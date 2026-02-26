package com.abs.casino.forms.game;

import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.forms.game.CommonBonusStartGameForm;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public class CommonFRBStartGameForm extends CommonBonusStartGameForm {
    private final static Logger LOG = LogManager.getLogger(CommonFRBStartGameForm.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.REAL;
    }

    @Override
    protected boolean isExist(long bonusId) {
        return FRBonusManager.getInstance().getById(this.bonusId) != null;
    }
}
