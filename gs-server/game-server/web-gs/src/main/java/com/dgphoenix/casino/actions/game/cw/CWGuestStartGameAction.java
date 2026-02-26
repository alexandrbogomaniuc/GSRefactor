package com.dgphoenix.casino.actions.game.cw;

/**
 * User: isirbis
 * Date: 08.10.14
 */

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.actions.enter.LanguageDetector;
import com.dgphoenix.casino.actions.game.BaseStartGameAction;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.MaintenanceModeException;
import com.dgphoenix.casino.common.exception.StartParameters;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.MobileDetector;
import com.abs.casino.entities.game.requests.StartGameRequest;
import com.abs.casino.entities.game.requests.StartGameResponse;
import com.dgphoenix.casino.exceptions.LoginErrorException;
import com.dgphoenix.casino.forms.game.cw.CWGuestStartGameForm;
import com.dgphoenix.casino.forms.login.cw.CWLoginForm;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.helpers.login.CWHelper;
import com.dgphoenix.casino.helpers.login.LoginHelper;
import com.dgphoenix.casino.helpers.login.serializers.CWSerializeLoginForm;
import com.dgphoenix.casino.sm.login.CWLoginRequest;
import com.dgphoenix.casino.sm.login.LoginResponse;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Migrate from CWGuestLogin
 */
public class CWGuestStartGameAction extends BaseStartGameAction<CWGuestStartGameForm, CWLoginRequest, StartGameRequest> {
    private static final Logger LOG = LogManager.getLogger(CWGuestStartGameAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, CWGuestStartGameForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        LoginResponse loginResponse;
        try {
            loginResponse = login(constructLoginParams(form));
        } catch (LoginErrorException e) {
            LOG.error("Could not login. Error code: " + e.getErrorCode() + ". Message: " + e.getMessage());
            addError(request, "error.login.internalError");
            return mapping.findForward(ERROR_FORWARD);
        }

        SessionHelper.getInstance().lock(loginResponse.getSessionInfo().getSessionId());
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            AccountInfo accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();

            String lang = form.getLang();

            StartGameResponse startGameResponse = startGame(constructStartGameParams(form, sessionInfo, accountInfo),
                    response);
            saveReferer(request, accountInfo);
            int gameId = (int) GameSessionPersister.getInstance().getGameSession(startGameResponse.getGameSessionId()).
                    getGameId();
            String userAgent = request.getHeader("User-Agent");
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            if (StringUtils.isTrimmedEmpty(lang)) {
                lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
            }
            boolean langNotFound;
            if (MobileDetector.isMobile(userAgent) && bankInfo.isUseSingleGameIdForAllDevices()) {
                langNotFound = !LanguageDetector.isLocalizationAvailable(lang, bankInfo, gameId, accountInfo, userAgent);
            } else {
                langNotFound = !LanguageDetector.isLocalizationAvailable(lang, bankInfo, gameId, accountInfo);
            }
            if (langNotFound) {
                if (bankInfo.isShowGameLocalizationError()) {
                    getLog().error("localization error");
                    addError(request, "error.login.localizationError");
                    return mapping.findForward(ERROR_FORWARD);
                } else {
                    lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                }
            }
            lang = LanguageDetector.resolveLanguageAlias(lang);

            GameMode mode = form.getGameMode();
            if (mode != GameMode.FREE) {
                applyLangToGameSession(startGameResponse.getGameSessionId(), lang);
            }
            ServerInfo serverInfo = assignServer(form.getBankId(), gameId, mode);
            response.addHeader("P3P", "CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");
            ActionRedirect forward;
            if (isMultiPlayerGame(form.getGameId())) {
                validateMpPass(request);
                AccountManager.getInstance().setFreeBalance(accountInfo, gameId);
                forward = getMultiPlayerForward(request, mode, bankInfo, sessionInfo.getSessionId(), lang,
                        true, gameId);
            } else {
                ActionForward actionForward = postProcessActions(mapping, request, response,
                        SessionHelper.getInstance().getTransactionData());
                if (actionForward != null) {
                    return actionForward;
                }
                forward = getForward(mapping, request, form, gameId, serverInfo.getHost(),
                        sessionInfo.getSessionId(), GAME_LAUNCHER_JSP, mode, lang, (short) serverInfo.getServerId(),
                        null, bankInfo, SessionHelper.getInstance().getTransactionData());
            }
            SessionHelper.getInstance().commitTransaction();
            SessionHelper.getInstance().markTransactionCompleted();
            return forward;
        } catch (MaintenanceModeException e) {
            getLog().warn(e.getMessage());
            request.setAttribute(StartParameters.class.getSimpleName(), e.getParameters());
            return new ActionForward(GameServerConfiguration.getInstance().getMaintenancePage());
        } catch (Throwable e) {
            LOG.error("process error", e);
            addError(request, "error.login.internalError");
            return mapping.findForward(ERROR_FORWARD);
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    private CWLoginRequest constructLoginParams(CWGuestStartGameForm form) throws Exception {
        CWSerializeLoginForm<CWLoginForm, CWGuestStartGameForm> serializeLoginForm = new CWSerializeLoginForm();
        CWLoginRequest loginRequest = serializeLoginForm.getLoginRequest(form);

        loginRequest.setIsGuest(true);

        return loginRequest;
    }

    protected StartGameRequest constructStartGameParams(CWGuestStartGameForm form, SessionInfo sessionInfo, AccountInfo accountInfo) {
        StartGameRequest startGameRequest = new StartGameRequest(sessionInfo, accountInfo, form, false);
        startGameRequest.setProfileId(form.getProfileId());

        return startGameRequest;
    }

    @Override
    protected LoginHelper getLoginHelper() {
        return CWHelper.getInstance();
    }

    protected ActionForward postProcessActions(ActionMapping mapping, HttpServletRequest request,
                                               HttpServletResponse response, ITransactionData transactionData) throws Exception {
        return null;
    }

    @Override
    protected ActionRedirect getForward(ActionMapping mapping, HttpServletRequest request, CWGuestStartGameForm actionForm,
                                        int gameId, String host, String sessionId, String startGamePage, GameMode mode,
                                        String lang, short serverId, StartGameResponse startGameResponse, BankInfo bankInfo, ITransactionData transactionData)
            throws CommonException {
        ActionRedirect redirect = super.getForward(mapping, request, actionForm, gameId, host, sessionId, startGamePage,
                mode, lang, serverId, startGameResponse, bankInfo, transactionData);

        if (!StringUtils.isTrimmedEmpty(actionForm.getProfileId())) {
            redirect.addParameter(BaseAction.PROFILE_ID, actionForm.getProfileId());
        }

        return redirect;
    }
}
