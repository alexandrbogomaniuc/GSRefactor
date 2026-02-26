package com.abs.casino.actions.game.tournament;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.actions.GameServerActionUtils;
import com.dgphoenix.casino.actions.enter.LanguageDetector;
import com.dgphoenix.casino.actions.enter.game.BaseStartGameAction;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.*;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.MaxBalanceTournamentPlayerDetails;
import com.dgphoenix.casino.common.promo.Status;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.dgphoenix.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.dgphoenix.casino.helpers.login.LoginHelper;
import com.dgphoenix.casino.promo.PromoCampaignManager;
import com.dgphoenix.casino.promo.persisters.CassandraMaxBalanceTournamentPersister;
import com.dgphoenix.casino.sm.IPlayerSessionManager;
import com.dgphoenix.casino.sm.PlayerSessionFactory;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: flsh
 * Date: 05.08.2020.
 */
public class TournamentStartGameAction extends BaseStartGameAction<TournamentStartGameForm> {
    private static final Logger LOG = LogManager.getLogger(TournamentStartGameAction.class);
    private final PromoCampaignManager campaignManager;
    private final CassandraMaxBalanceTournamentPersister maxBalanceTournamentPersister;

    public TournamentStartGameAction() {
        campaignManager = (PromoCampaignManager) GameServerComponentsHelper.getPromoCampaignManager();
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        CassandraPersistenceManager persistenceManager = applicationContext
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        maxBalanceTournamentPersister = persistenceManager.getPersister(CassandraMaxBalanceTournamentPersister.class);
    }

    @Override
    protected ActionForward process(ActionMapping mapping, TournamentStartGameForm actionForm,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            LOG.info("process:: {}, host={}", actionForm, request.getRemoteHost());
            GameServerActionUtils.initializeHttpRequestContext(request);

            long tournamentId = actionForm.getTournamentId();
            IPromoCampaign promoCampaign = campaignManager.getPromoCampaign(tournamentId);
            if (promoCampaign == null || promoCampaign.getStatus() != Status.STARTED) {
                throw new CommonException("incorrect parameters::tournamentId is invalid");
            }
            long gameId = Long.parseLong(actionForm.getGameId());
            if (!promoCampaign.getGameIds().contains(gameId)) {
                CommonException exception = new CommonException("incorrect parameters::gameId is not found for this " +
                        "tournamentId=" + tournamentId);
                addErrorWithPersistence(request, "error.login.internalServerError", exception,
                        System.currentTimeMillis());
                throw exception;
            }
            String lang = actionForm.getLang();
            String externalId = null;
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(actionForm.getBankId());
            boolean isLocked;
            if (StringUtils.isTrimmedEmpty(actionForm.getSessionId())) {
                final CommonWalletAuthResult authResult = getAuthInfo(actionForm, actionForm.getToken(), bankInfo,
                        request.getRemoteHost(), request);
                externalId = authResult.getUserId();
                SessionHelper.getInstance().lock(actionForm.getBankId(), externalId);
                isLocked = true;
            } else {
                SessionHelper.getInstance().lock(actionForm.getSessionId());
                isLocked = true;
            }

            try {
                SessionHelper.getInstance().openSession();
                AccountInfo accountInfo;
                SessionInfo sessionInfo;
                if (externalId != null) {
                    accountInfo = AccountManager.getInstance().getAccountInfo(actionForm.getSubCasinoId(),
                            actionForm.getBankId(), externalId);
                } else {
                    //use must be logged in
                    sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                    if (sessionInfo == null || !sessionInfo.getSessionId().equals(actionForm.getSessionId())) {
                        LOG.debug("Session not found, sessionInfo={}, actionForm={}", sessionInfo, actionForm);
                        throw new CommonException("Session not found");
                    }
                    accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();
                }
                if (accountInfo == null) {
                    CommonException exception = new CommonException("Account not found " +
                            "externalId=" + externalId);
                    addErrorWithPersistence(request, "error.login.internalServerError", exception, System.currentTimeMillis());
                    throw exception;
                }
                MaxBalanceTournamentPlayerDetails details =
                        maxBalanceTournamentPersister.getForAccount(accountInfo.getId(), tournamentId);
                if (details == null) {
                    LOG.error("playerDetails not found, tournamentId={}, accountId={}",
                            tournamentId, accountInfo.getId());
                    throw new CommonException("Player not joined to tournament");
                }

                checkAvailableGameInfo(gameId, bankInfo, "");

                IBaseGameInfo info = BaseGameCache.getInstance().getGameInfo(bankInfo.getId(), gameId, "");
                if (info == null || !info.isEnabled()) {
                    LOG.error("Game {} is not available for bank {}", gameId, bankInfo.getId());
                    throw new CommonException("Game is not available");
                }

                GameSession oldGameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
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
                    LOG.info("login with sessionId: {}", sessionInfo.getSessionId());
                } else {
                    sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                }
                if (StringUtils.isTrimmedEmpty(lang)) {
                    lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                }
                saveReferer(request, accountInfo);
                lang = LanguageDetector.resolveLanguageAlias(lang);
                validateMpPass(request, actionForm.getBankId());
                GameServer.getInstance().checkMaintenanceMode(GameMode.REAL, lang, accountInfo, gameId);
                checkPendingOperations(accountInfo, gameId, sessionInfo, null, GameMode.REAL, true);
                checkTournamentPendingOperations(accountInfo, sessionInfo);
                if (!LanguageDetector.isLocalizationAvailable(lang, bankInfo, gameId, accountInfo)) {
                    if (bankInfo.isShowGameLocalizationError()) {
                        getLog().error("localization error");
                        addError(request, "error.login.localizationError");
                        return mapping.findForward(ERROR_FORWARD);
                    } else {
                        lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, accountInfo);
                    }
                }
                ActionRedirect mpRedirect = getMultiPlayerForward(actionForm, request, GameMode.REAL, bankInfo,
                        sessionInfo.getSessionId(), lang, gameId);
                mpRedirect.addParameter(BaseAction.PARAM_TOURNAMENT_ID, promoCampaign.getId());
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
                return mpRedirect;

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
        } catch (UnknownCurrencyException e) {
            getLog().error(e.getMessage());
            addError(request, "error.login.currencyCodeValidationError");
            return mapping.findForward(ERROR_FORWARD);
        } catch (InvalidCurrencyRateException e) {
            getLog().error(e.getMessage());
            addError(request, "error.login.invalidCurrencyRate");
            return mapping.findForward(ERROR_FORWARD);
        } catch (CurrencyMismatchException e) {
            getLog().error(e.getMessage());
            return mapping.findForward(ERROR_FORWARD);
        } catch (Exception e) {
            getLog().error("process error", e);
            if (e instanceof WalletException && (actionForm.getBankId() == 121 || actionForm.getBankId() == 221
                    || actionForm.getBankId() == 226)) {
                addError(request, "error.login.walletError121", String.valueOf(((WalletException) e).getAccountId()));
            } else {
                addErrorWithPersistence(request, "error.login.internalError", e, System.currentTimeMillis());
            }
            return mapping.findForward(ERROR_FORWARD);
        }
    }

    private void checkTournamentPendingOperations(AccountInfo accountInfo, SessionInfo sessionInfo) throws CommonException {
        checkPendingOperations(accountInfo, 1, sessionInfo, null, GameMode.REAL, true);
    }

}
