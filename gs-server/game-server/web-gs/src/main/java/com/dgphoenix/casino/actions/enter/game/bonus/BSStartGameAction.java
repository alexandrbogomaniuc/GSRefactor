package com.abs.casino.actions.enter.game.bonus;

import com.dgphoenix.casino.account.AccountManager;
import com.abs.casino.actions.enter.LanguageDetector;
import com.abs.casino.actions.enter.game.routing.BonusFrbServiceRoutingBridge;
import com.dgphoenix.casino.actions.enter.game.BaseStartGameAction;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.*;
import com.abs.casino.common.exception.CurrencyMismatchException;
import com.abs.casino.common.exception.InvalidCurrencyRateException;
import com.abs.casino.common.exception.UnknownCurrencyException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.abs.casino.common.web.login.apub.GameServerResponse;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.IBonusClient;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAuthResult;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyManager;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.gs.persistance.PlayerSessionPersister;
import com.dgphoenix.casino.helpers.login.LoginHelper;
import com.dgphoenix.casino.sm.IPlayerSessionManager;
import com.dgphoenix.casino.sm.PlayerSessionFactory;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BSStartGameAction extends BaseStartGameAction<BSStartGameForm> {
    private static final Logger LOG = LogManager.getLogger(BSStartGameAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, BSStartGameForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        try {
            LOG.info("loginForBonusGame:: {}, host={}", actionForm, request.getRemoteHost());

            if (StringUtils.isTrimmedEmpty(actionForm.getToken()) && actionForm.getGameMode() == GameMode.BONUS) {
                throw new CommonException("incorrect parameters: token='" + actionForm.getToken() +
                        "', actionForm=" + actionForm.getGameMode());
            }

            Bonus bonus = BonusManager.getInstance().getById(actionForm.getBonusId());
            if (bonus == null || bonus.getStatus() != BonusStatus.ACTIVE) {
                throw new CommonException("incorrect parameters::bonusId is invalid");
            }
            if (!bonus.isReady()) {
                CommonException exception = new CommonException("incorrect parameters::bonusId is not ready yet, " +
                        "bonusId=" + bonus.getId());
                addErrorWithPersistence(request, "error.login.bonusNotStartedYet", exception, System.currentTimeMillis());
                return mapping.findForward(ERROR_FORWARD);
            }

            BonusAuthResult result = getBonusAuthResult(actionForm);
            String externalId = result.getUserId();
            String currency = result.getCurrency();
            String lang = actionForm.getLang();
            long gameId = Long.parseLong(actionForm.getGameId());
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(actionForm.getBankId());
            boolean isLocked = true;
            if (actionForm.getSessionId() == null) {
                SessionHelper.getInstance().lock(actionForm.getBankId(), externalId);
            } else {
                SessionHelper.getInstance().lock(actionForm.getSessionId());
            }

            try {
                SessionHelper.getInstance().openSession();

                AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(actionForm.getSubCasinoId(),
                        actionForm.getBankId(), externalId);
                if (accountInfo == null) {
                    accountInfo = saveAccount(null, externalId, currency, result.getUserName(), result.getFirstName(),
                            result.getLastName(), result.getEmail(), result.getCountryCode(), actionForm, true);
                    if (bankInfo.isAddTokenMode()) {
                        accountInfo.setFinsoftSessionId(actionForm.getToken());
                    }
                }

                String authCurrency = result.getCurrency();
                CurrencyManager.getInstance().setupCurrency(authCurrency, authCurrency, bankInfo.getId());

                LOG.info("loginForBonusGame:: bonus={}, gameId={}", bonus, actionForm.getGameId());
                if (bonus.getAccountId() != accountInfo.getId()) {
                    CommonException exception = new CommonException("incorrect parameters:: accountId mismatch, " +
                            "accountId=" + accountInfo.getId());
                    addErrorWithPersistence(request, "error.login.internalServerError", exception, System.currentTimeMillis());
                    throw exception;
                }

                if (!BonusManager.getInstance().bonusIsValidForGameId(bonus, accountInfo, gameId)) {
                    CommonException exception = new CommonException("incorrect parameters::gameId is not found for this " +
                            "bonusId=" + bonus.getId());
                    addErrorWithPersistence(request, "error.login.internalServerError", exception, System.currentTimeMillis());
                    throw exception;
                }

                shadowBonusFrbCheck(actionForm.getBankId(), accountInfo.getId(), bonus.getId(), actionForm.getToken());

                boolean multiPlayerGame = isMultiPlayerGame(gameId);
                GameSession oldGameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                SessionInfo sessionInfo;
                //always close MQ game
                if (GameServer.getInstance().needCloseMultiplayerGame(oldGameSession, bankInfo, -1)) {
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                    SessionHelper.getInstance().clearWithUnlock();
                    isLocked = false;
                    LoginHelper.performMaxQuestSitOut(accountInfo, oldGameSession, bankInfo);
                    SessionHelper.getInstance().lock(accountInfo.getId());
                    isLocked = true;
                    SessionHelper.getInstance().openSession();
                    accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();
                }
                if (actionForm.getSessionId() == null) {
                    IPlayerSessionManager psm = PlayerSessionFactory.getInstance().getPlayerSessionManager(actionForm.getBankId());
                    sessionInfo = psm.login(accountInfo, actionForm.getToken(), request.getRemoteHost(),
                            actionForm.getClientType());
                } else {
                    sessionInfo = PlayerSessionPersister.getInstance().getSessionInfo();
                }
                String sessionId = sessionInfo.getSessionId();

                LOG.info("login with sessionId: {}", sessionId);

                if (StringUtils.isTrimmedEmpty(lang)) {
                    lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                }
                saveReferer(request, accountInfo);
                lang = LanguageDetector.resolveLanguageAlias(lang);
                if (multiPlayerGame) {
                    validateMpPass(request, actionForm.getBankId());
                    GameServer.getInstance().checkMaintenanceMode(GameMode.BONUS, lang, accountInfo, gameId);
                    checkPendingOperations(accountInfo, gameId, sessionInfo, null, GameMode.BONUS, true);
                    checkPendingOperations(accountInfo, gameId, sessionInfo, null, GameMode.REAL, true);
                    if (!LanguageDetector.isLocalizationAvailable(lang, bankInfo, gameId, accountInfo)) {
                        if (bankInfo.isShowGameLocalizationError()) {
                            getLog().error("localization error");
                            addError(request, "error.login.localizationError");
                            return mapping.findForward(ERROR_FORWARD);
                        } else {
                            lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                        }
                    }
                    ActionRedirect mpRedirect = getMultiPlayerForward(actionForm, request, GameMode.BONUS, bankInfo,
                            sessionInfo.getSessionId(), lang, gameId);
                    mpRedirect.addParameter(BaseAction.PARAM_BONUS_ID, bonus.getId());
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                    return mpRedirect;
                }
                GameServerResponse serverResponse;
                try {
                    serverResponse = startGame(accountInfo, sessionInfo, actionForm, response, lang, false);
                } catch (BonusException e) {
                    LOG.error("Game cannot be started, invalid bonus after close old GameSession " +
                            "(but need commit transaction)", e);
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                    throw e;
                }

                gameId = GameSessionPersister.getInstance().getGameSession(serverResponse.getGameSessionId()).getGameId();
                if (!LanguageDetector.isLocalizationAvailable(lang, bankInfo, gameId, accountInfo)) {
                    if (bankInfo.isShowGameLocalizationError()) {
                        addError(request, "error.login.localizationError");
                        throw new CommonException("localization error");
                    } else {
                        lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                    }
                }

                GameMode mode = actionForm.getGameMode();
                if (mode != GameMode.FREE) {
                    applyLangToGameSession(serverResponse.getGameSessionId(), lang);
                }
                ServerInfo serverInfo = assignServer(actionForm.getBankId(), gameId, mode);
                ActionRedirect forward = getForward(mapping, request, actionForm, gameId, serverInfo.getHost(), sessionId,
                        GAME_LAUNCHER_JSP, mode, lang, serverInfo.getServerId(), bankInfo, null, false,
                        SessionHelper.getInstance().getTransactionData());
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                return forward;
            } finally {
                if (isLocked) {
                    SessionHelper.getInstance().clearWithUnlock();
                } else {
                    getLog().debug("not locked, skip SessionHelper.getInstance().clearWithUnlock()");
                }
            }
        } catch (MaintenanceModeException e) {
            getLog().warn(e.getMessage());
            request.setAttribute(StartParameters.class.getSimpleName(), e.getParameters());
            return new ActionForward(GameServerConfiguration.getInstance().getMaintenancePage());
        } catch (CurrencyMismatchException e) {
            getLog().error(e.getMessage());
            return mapping.findForward(ERROR_FORWARD);
        } catch (UnknownCurrencyException e) {
            getLog().error(e.getMessage());
            addError(request, "error.login.currencyCodeValidationError");
            return mapping.findForward(ERROR_FORWARD);
        } catch (InvalidCurrencyRateException e) {
            getLog().error(e.getMessage());
            addError(request, "error.login.invalidCurrencyRate");
            return mapping.findForward(ERROR_FORWARD);
        } catch (Exception e) {
            LOG.error("process error", e);
            addError(request, "error.login.internalError");
            return mapping.findForward(ERROR_FORWARD);
        }
    }

    protected BonusAuthResult getBonusAuthResult(BSStartGameForm actionForm) throws CommonException {
        IBonusClient client = BonusManager.getInstance().getClient(actionForm.getBankId());
        return client.authenticate(actionForm.getToken());
    }

    @Override
    protected Long validateBonusIdParam(BSStartGameForm form, GameMode mode, AccountInfo accountInfo)
            throws CommonException {
        if (!mode.equals(GameMode.BONUS)) {
            return null;
        }
        long bonusId = form.getBonusId();
        Bonus bonus = BonusManager.getInstance().getById(bonusId);
        if (bonus == null || !bonus.getStatus().equals(BonusStatus.ACTIVE)) {
            throw new BonusException("bonus is null or not active");
        }

        if (bonus.getAccountId() != accountInfo.getId()) {
            throw new BonusException("incorrect parameters:: accountId mismatch, " +
                    "accountId=" + accountInfo.getId());
        }

        return bonusId;
    }

    @Override
    protected void persistError(HttpServletRequest request, Exception exception, long exceptionTime) {
        errorPersisterHelper.persistStartGameActionError(request, exception, exceptionTime);
    }

    private void shadowBonusFrbCheck(long bankId, long accountId, long frbId, String traceToken) {
        try {
            BonusFrbServiceRoutingBridge.RouteDecision routeDecision =
                    BonusFrbServiceRoutingBridge.getInstance().decide(bankId);
            BonusFrbServiceRoutingBridge.getInstance().shadowCheckFrb(
                    routeDecision,
                    bankId,
                    String.valueOf(accountId),
                    String.valueOf(frbId),
                    traceToken
            );
        } catch (Exception e) {
            LOG.warn("bonus-frb-service shadow check failed (ignored): bankId={}, accountId={}, frbId={}, reason={}",
                    bankId, accountId, frbId, e.getMessage());
        }
    }
}
