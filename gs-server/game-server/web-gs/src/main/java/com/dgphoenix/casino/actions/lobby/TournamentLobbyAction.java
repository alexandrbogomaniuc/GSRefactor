package com.abs.casino.actions.lobby;

import com.dgphoenix.casino.account.AccountManager;
import com.abs.casino.actions.GameServerActionUtils;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.entities.lobby.LoginHelper;
import com.abs.casino.exceptions.LoginErrorException;
import com.abs.casino.forms.lobby.TournamentLobbyForm;
import com.abs.casino.sm.login.LoginResponse;
import com.abs.casino.support.ErrorPersisterHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TournamentLobbyAction extends STLobbyAction<TournamentLobbyForm> {

    private static final Logger LOG = LogManager.getLogger(TournamentLobbyAction.class);

    private final ErrorPersisterHelper errorPersisterHelper;

    public TournamentLobbyAction() {
        errorPersisterHelper = ApplicationContextHelper.getApplicationContext().getBean(ErrorPersisterHelper.class);
    }

    @Override
    protected ActionForward process(ActionMapping mapping, TournamentLobbyForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        GameServerActionUtils.initializeHttpRequestContext(request);
        String sessionId = form.getSessionId();
        AccountInfo account;
        if (!StringUtils.isTrimmedEmpty(form.getSID()) || !StringUtils.isTrimmedEmpty(sessionId)) {
            if (sessionId == null) {
                sessionId = form.getSID();
            }
            try {
                account = getAccountInfoBySessionId(sessionId);
            } catch (Exception e) {
                if (sessionId != null) {
                    errorPersisterHelper.persistTournamentLobbyActionError(sessionId, form.getToken(), form.getBankId(), e);
                }
                throw e;
            }
        } else {
            LoginHelper helper = getHelper(form);
            LoginResponse loginResponse;
            try {
                loginResponse = getLoginResponse(helper, form, request);
            } catch (LoginErrorException e) {
                LOG.error("Could not login. form=" + form, e);
                addErrorWithPersistence(request, "error.login.internalError", e, System.currentTimeMillis());
                return mapping.findForward(ERROR_FORWARD);
            }
            sessionId = loginResponse.getSessionInfo().getSessionId();
            if (request.isSecure()) {
                setForceHttps(sessionId);
            }
            account = loginResponse.getAccountInfo();
        }

        try {
            LOG.debug("form={}", form);
            Integer bankId = StringUtils.isTrimmedEmpty(form.getSID()) ? form.getBankId() : account.getBankId();

            if (account == null || account.isGuest()) {
                LOG.error("Tournament requires real account");
                return mapping.findForward(ERROR_FORWARD);
            }

            request.setAttribute(ACCOUNT_EXTERNAL_ID, account.getExternalId());
            request.setAttribute(CURRENCY_CODE, account.getCurrency().getCode());
            request.setAttribute(BANK_ID_ATTRIBUTE, bankId);
            request.setAttribute(SESSION_ID_ATTRIBUTE, sessionId);
            request.setAttribute(LANG_ID_ATTRIBUTE, form.getLang());
            request.setAttribute(TOKEN_ATTRIBUTE, form.getToken());
            request.setAttribute(REAL_MODE_URL, form.getRealModeUrl());
            request.setAttribute(KEY_CDN, getCdnUrl(request, bankId));
            request.setAttribute(GAME_ID_ATTRIBUTE, form.getGameId());
            request.setAttribute(SHOW_BATTLEGROUND_TAB, form.getCurrentMode().equals(GameMode.REAL.getModePath())
                    && form.isShowBattlegroundTab());

            return mapping.findForward("success");
        } catch (Exception e) {
            LOG.error("::execute() exception", e);
            if (sessionId != null) {
                errorPersisterHelper.persistTournamentLobbyActionError(sessionId, form.getToken(), form.getBankId(), e);
            }
            return new ActionForward("/error_pages/sessionerror.jsp");
        }
    }

    private void setForceHttps(String sessionId) throws CommonException {
        SessionHelper sessionHelper = SessionHelper.getInstance();
        sessionHelper.lock(sessionId);
        try {
            sessionHelper.openSession();
            SessionInfo sessionInfo = sessionHelper.getTransactionData().getPlayerSession();
            sessionInfo.setForceHttps(true);
            sessionHelper.commitTransaction();
            sessionHelper.markTransactionCompleted();
        } finally {
            sessionHelper.clearWithUnlock();
        }
    }

    private LoginHelper getHelper(TournamentLobbyForm form) {
        return LoginHelper.CWv3;
    }

    private AccountInfo getAccountInfoBySessionId(String sessionId) throws CommonException {
        AccountInfo account;
        SessionHelper sessionHelper = SessionHelper.getInstance();
        sessionHelper.lock(sessionId);
        try {
            sessionHelper.openSession();
            ITransactionData transactionData = sessionHelper.getTransactionData();
            account = AccountManager.getInstance().getAccountInfo(transactionData.getAccountId());
        } finally {
            sessionHelper.clearWithUnlock();
        }
        return account;
    }

    private String getCdnUrl(HttpServletRequest request, Integer bankId) {
        if (bankId != null) {
            String cdn = request.getParameter(BaseAction.KEY_CDN);
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (!StringUtils.isTrimmedEmpty(cdn)) {
                return bankInfo.getCdnUrlsMap().get(cdn);
            } else if (!bankInfo.getCdnUrlsMap().isEmpty() && bankInfo.isCdnForceAuto()) {
                return bankInfo.getCdnUrlsMap().values().iterator().next();
            }
        }
        return "";
    }

    @Override
    protected void persistError(HttpServletRequest request, Exception exception, long exceptionTime) {
        errorPersisterHelper.persistStartGameActionError(request, exception, exceptionTime);
    }
}

