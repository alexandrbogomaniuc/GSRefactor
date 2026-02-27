package com.abs.casino.actions.enter.cw;

import com.dgphoenix.casino.account.AccountManager;
import com.abs.casino.actions.enter.AccountInfoAndSessionInfoPair;
import com.abs.casino.actions.enter.LanguageDetector;
import com.dgphoenix.casino.actions.enter.game.BaseStartGameAction;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.MaintenanceModeException;
import com.dgphoenix.casino.common.exception.StartParameters;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.MobileDetector;
import com.abs.casino.common.web.login.apub.GameServerResponse;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: plastical
 * Date: 23.04.2010
 */
public class CWGuestLogin extends BaseStartGameAction<CWGuestLoginForm> {
    private static final Logger LOG = LogManager.getLogger(CWGuestLogin.class);

    @Override
    protected ActionForward process(ActionMapping mapping, CWGuestLoginForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        AccountInfoAndSessionInfoPair infoPair;
        try {
            Currency currency = actionForm.getCurrency();
            infoPair = loginGuest(actionForm, request, currency);
            SessionInfo sessionInfo = infoPair.getSessionInfo();
            String lang = actionForm.getLang();
            long gameId = Long.parseLong(actionForm.getGameId());
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(actionForm.getBankId());
            AccountInfo accountInfo = infoPair.getAccount();
            if (StringUtils.isTrimmedEmpty(lang)) {
                lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
            }
            String userAgent = request.getHeader("User-Agent");
            lang = LanguageDetector.resolveLanguageAlias(lang);

            if (isMultiPlayerGame(gameId)) {
                try {
                    validateMpPass(request, actionForm.getBankId());
                } catch (MaintenanceModeException e) {
                    return mapping.findForward(GameServerConfiguration.getInstance().getMaintenancePage());
                }
                if (BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).isBattleGroundsMultiplayerGame()) {
                    getLog().error("battle is not allowed");
                    addError(request, "error.login.incorrectParameters");
                    return mapping.findForward(ERROR_FORWARD);
                }
                AccountManager.getInstance().setFreeBalance(accountInfo, gameId);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                return getMultiPlayerForward(actionForm, request, GameMode.FREE, bankInfo,
                        sessionInfo.getSessionId(), lang, gameId);
            }

            GameServerResponse serverResponse = startGame(infoPair.getAccount(), sessionInfo, actionForm,
                    response, lang, false);
            GameMode mode = actionForm.getGameMode();
            gameId = GameSessionPersister.getInstance().getGameSession(serverResponse.getGameSessionId()).getGameId();
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
            if (mode != GameMode.FREE) {
                applyLangToGameSession(serverResponse.getGameSessionId(), lang);
            }
            ServerInfo serverInfo = assignServer(actionForm.getBankId(), gameId, mode);
            response.addHeader("P3P", "CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");
            String currencyCode = (currency == null) ? null : currency.getCode();
            ActionRedirect forward = getForward(mapping, request, actionForm, gameId, serverInfo.getHost(),
                    sessionInfo.getSessionId(), GAME_LAUNCHER_JSP, mode, lang, serverInfo.getServerId(),
                    bankInfo, currencyCode, false, SessionHelper.getInstance().getTransactionData());
            SessionHelper.getInstance().commitTransaction();
            SessionHelper.getInstance().markTransactionCompleted();
            return forward;
        } catch (MaintenanceModeException e) {
            getLog().warn(e.getMessage());
            request.setAttribute(StartParameters.class.getSimpleName(), e.getParameters());
            return new ActionForward(gameServerConfiguration.getMaintenancePage());
        } catch (Throwable t) {
            LOG.error("process error", t);
            addError(request, "error.login.internalError");
            return mapping.findForward(ERROR_FORWARD);
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }
}
