package com.dgphoenix.casino.web.history;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.helpers.login.CWv3Helper;
import com.dgphoenix.casino.sm.login.GameLoginRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mic on 16.05.14.
 */
public class GameHistoryXMLListFormV2 extends GameHistoryXMLListForm {
    private static final Logger LOG = LogManager.getLogger(GameHistoryXMLListFormV2.class);
    private static final long serialVersionUID = 4569326751871351344L;
    private String token;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        String token = request.getParameter("token");
        if (StringUtils.isTrimmedEmpty(token)) {
            errors.add("token", new ActionMessage("error.login.badToken"));
            return errors;
        }
        setToken(token);

        String strBankId = request.getParameter("bankId");
        if (StringUtils.isTrimmedEmpty(strBankId)) {
            errors.add("bankId", new ActionMessage("error.gameInfoForm.emptyBankId"));
            return errors;
        }

        Integer bankId = Integer.valueOf(strBankId);
        if (BankInfoCache.getInstance().getBankInfo(bankId) == null) {
            errors.add("bankId", new ActionMessage("error.gameInfoForm.incorrectBankId", "bankId", bankId));
            return errors;
        }
        this.bankId = bankId;

        try {
            String sessionId = getSession(bankId, request);
            setSessionId(sessionId);
        } catch (Exception e) {
            LOG.error("Error while getting sessionId for token: " + token + "bankId: " + bankId, e);
            errors.add("sessionId", new ActionMessage("error.login.badToken"));
            return errors;
        }

        errors = super.validate(mapping, request);
        return errors;
    }

    private String getSession(long bankId, HttpServletRequest request) throws Exception {
        GameLoginRequest loginRequest = new GameLoginRequest();
        loginRequest.setToken(getToken());
        loginRequest.setSubCasinoId((short) BankInfoCache.getInstance().getBankInfo(bankId).getSubCasinoId());
        loginRequest.setBankId((int) bankId);
        loginRequest.setGameMode(GameMode.FREE);
        loginRequest.setRemoteHost(request.getRemoteAddr());
        try {
            return CWv3Helper.getInstance().login(loginRequest).getSessionInfo().getSessionId();
        } catch (Exception e) {
            LOG.error("getSession error", e);
            throw new Exception(e);
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
