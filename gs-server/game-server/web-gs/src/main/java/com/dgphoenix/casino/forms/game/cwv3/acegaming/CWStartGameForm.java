package com.abs.casino.forms.game.cwv3.acegaming;

import com.abs.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.forms.game.cwv3.AbstractCWStartGameForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

public class CWStartGameForm extends AbstractCWStartGameForm {
    private final static Logger LOG = LogManager.getLogger(CWStartGameForm.class);

    @Override
    public GameMode getGameMode() {
        return GameMode.REAL;
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

        ActionErrors actionErrors = super.validate(mapping, request);

        String sessionid = BaseAction.extractRequestParameterIgnoreCase(request, "sessionid");
        if (StringUtils.isTrimmedEmpty(sessionid)) {
            actionErrors.add("empty_credentials", new ActionMessage("error.login.incorrectCredentials"));
            return actionErrors;
        }
        this.token = sessionid;

        return actionErrors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CWStartGameForm");
        sb.append("{}");
        return sb.toString();
    }
}
