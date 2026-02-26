package com.abs.casino.actions.game.bonus;

import com.dgphoenix.casino.actions.enter.LanguageDetector;
import com.dgphoenix.casino.actions.game.BaseStartGameAction;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.MaintenanceModeException;
import com.dgphoenix.casino.common.exception.StartParameters;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.entities.game.requests.BonusStartGameRequest;
import com.abs.casino.entities.game.requests.StartGameResponse;
import com.dgphoenix.casino.forms.game.CommonStartGameForm;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.helpers.login.LoginHelper;
import com.dgphoenix.casino.sm.login.BonusGameLoginRequest;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: isirbis
 * Date: 09.10.14
 */
public abstract class AbstractBSStartGameAction<F extends CommonStartGameForm> extends BaseStartGameAction<F, BonusGameLoginRequest, BonusStartGameRequest> {

    @Override
    protected LoginHelper getLoginHelper() {
        return null;
    }

    protected ActionForward startGame(ActionMapping mapping, F form, HttpServletRequest request,
                                      HttpServletResponse response, String sessionId)
            throws Exception {

        String lang = form.getLang();
        int gameId;
        GameMode mode = form.getGameMode();

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());

        StartGameResponse startGameResponse;
        SessionHelper.getInstance().lock(sessionId);
        boolean isLocked = true;
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            AccountInfo accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();
            GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
            //always close MQ game
            if (GameServer.getInstance().needCloseMultiplayerGame(gameSession, bankInfo, -1)) {
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                SessionHelper.getInstance().clearWithUnlock();
                isLocked = false;
                LoginHelper.performMaxQuestSitOut(accountInfo, gameSession, bankInfo);
                SessionHelper.getInstance().lock(sessionId);
                isLocked = true;
                SessionHelper.getInstance().openSession();
                accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();
                sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            }
            startGameResponse = startGame(createStartGameRequest(sessionInfo, accountInfo, form), response);
            saveReferer(request, accountInfo);
            gameId = (int) GameSessionPersister.getInstance().
                    getGameSession(startGameResponse.getGameSessionId()).getGameId();

            if (StringUtils.isTrimmedEmpty(lang)) {
                lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
            }
            if (!LanguageDetector.isLocalizationAvailable(lang, bankInfo, gameId, accountInfo)) {
                if (bankInfo.isShowGameLocalizationError()) {
                    LOG.error("localization error");
                    addError(request, "error.login.localizationError");
                    return mapping.findForward(ERROR_FORWARD);
                } else {
                    lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                }
            }
            lang = LanguageDetector.resolveLanguageAlias(lang);

            if (mode != GameMode.FREE) {
                applyLangToGameSession(startGameResponse.getGameSessionId(), lang);
            }

            sessionId = sessionInfo.getSessionId();

            ActionRedirect forward = getForward(mapping, request, form, gameId, GameServer.getInstance().getHost(),
                    sessionId, GAME_LAUNCHER_JSP, mode, lang, (short) GameServer.getInstance().getServerId(),
                    startGameResponse, bankInfo, SessionHelper.getInstance().getTransactionData());

            SessionHelper.getInstance().commitTransaction();
            SessionHelper.getInstance().markTransactionCompleted();
            return forward;
        } catch (MaintenanceModeException e) {
            getLog().warn(e.getMessage());
            request.setAttribute(StartParameters.class.getSimpleName(), e.getParameters());
            return new ActionForward(GameServerConfiguration.getInstance().getMaintenancePage());
        } catch (Exception e) {
            LOG.error("Could not start game", e);
            addErrorWithPersistence(request, "error.common.internalError", e, System.currentTimeMillis());
            return mapping.findForward(ERROR_FORWARD);
        } finally {
            if (isLocked) {
                SessionHelper.getInstance().clearWithUnlock();
            } else {
                LOG.debug("isLocked=" + isLocked + ", skip SessionHelper.getInstance().clearWithUnlock()");
            }
        }


    }

    protected abstract BonusStartGameRequest createStartGameRequest(SessionInfo sessionInfo, AccountInfo accountInfo, F form);

    @Override
    protected void persistError(HttpServletRequest request, Exception exception, long exceptionTime) {
        errorPersisterHelper.persistStartGameActionError(request, exception, exceptionTime);
    }
}
