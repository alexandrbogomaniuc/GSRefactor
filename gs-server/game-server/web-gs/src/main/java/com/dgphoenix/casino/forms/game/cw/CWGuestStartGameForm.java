package com.abs.casino.forms.game.cw;

import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.forms.game.CommonStartGameForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * User: plastical
 * Date: 23.04.2010
 */
public class CWGuestStartGameForm extends CommonStartGameForm {
    private final static Logger LOG = LogManager.getLogger(CWGuestStartGameForm.class);

    @Override
    public GameMode getGameMode() {
        return GameMode.FREE;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        setCheckToken(false);
        setCheckGameMode(false);

        return super.validate(mapping, request);
    }
}
