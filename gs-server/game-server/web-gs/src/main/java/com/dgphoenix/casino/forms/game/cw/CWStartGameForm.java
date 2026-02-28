package com.abs.casino.forms.game.cw;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.forms.game.CommonStartGameForm;
import com.dgphoenix.casino.gs.GameServer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: plastical
 * Date: 05.04.2010
 */
public class CWStartGameForm extends CommonStartGameForm {
    private final static Logger LOG = LogManager.getLogger(CWStartGameForm.class);

    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public boolean isLaunchChecked() {
        return false;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        setCheckToken(false);
        ActionErrors actionErrors = super.validate(mapping, request);
        if (!actionErrors.isEmpty()) {
            return actionErrors;
        }
        this.sessionId = BaseAction.extractRequestParameterIgnoreCase(request, "sessionId");
        if (StringUtils.isTrimmedEmpty(sessionId)) {
            getLogger().warn("sessionId is empty");
            actionErrors.add("empty_credentials", new ActionMessage("error.login.incorrectCredentials"));
            return actionErrors;
        }
        final int serverId = StringIdGenerator.extractServerId(sessionId);
        if (GameServer.getInstance().getServerId() != serverId) {
            getLogger().warn("Found foreign player session: " + sessionId);
            //return actionErrors;
        }
        setBankInfo(BankInfoCache.getInstance().getBankInfo(getBankId()));
        return actionErrors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CWStartGameForm");
        sb.append("[sessionId='").append(sessionId).append('\'');
        sb.append(", gameId=").append(gameId);
        sb.append(']');
        return sb.toString();
    }
}
