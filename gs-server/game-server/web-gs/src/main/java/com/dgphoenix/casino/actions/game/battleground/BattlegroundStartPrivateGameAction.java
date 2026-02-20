package com.dgphoenix.casino.actions.game.battleground;

import com.dgphoenix.casino.actions.enter.LanguageDetector;
import com.dgphoenix.casino.actions.enter.game.BaseStartGameAction;
import com.dgphoenix.casino.cassandra.persist.mp.BattlegroundPrivateRoomSetting;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.MaintenanceModeException;
import com.dgphoenix.casino.common.exception.StartParameters;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.entities.lobby.LoginHelper;
import com.dgphoenix.casino.exceptions.LoginErrorException;
import com.dgphoenix.casino.gs.socket.mq.BattlegroundService;
import com.dgphoenix.casino.sm.login.GameLoginRequest;
import com.dgphoenix.casino.sm.login.LoginResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BattlegroundStartPrivateGameAction extends BaseStartGameAction<BattlegroundStartPrivateGameForm> {
    private static final Logger LOG = LogManager.getLogger(BattlegroundStartPrivateGameAction.class);

    private final BattlegroundService battlegroundService;

    public BattlegroundStartPrivateGameAction() {
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        this.battlegroundService = applicationContext.getBean(BattlegroundService.class);
    }

    @Override
    protected ActionForward process(ActionMapping mapping, BattlegroundStartPrivateGameForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {

        getLog().debug("BattlegroundStartPrivateGameAction process: enter process mapping={}, form={}, request={}", mapping, form, request);

        try {
            LOG.debug("Join private battleground room request: {}", form);
            BattlegroundPrivateRoomSetting privateRoomSettings = battlegroundService.getPrivateRoomSettings(form.getPrivateRoomId());
            if (privateRoomSettings == null) {
                throw new CommonException(String.format("Private room not found. privateRoomId: %s, bankId: %d", form.getPrivateRoomId(), form.getBankId()));
            }

            long bankId = privateRoomSettings.getBankId();
            validateMpPass(request, (int) bankId);
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null) {
                throw new CommonException("BankInfo is null for settings: " + privateRoomSettings);
            }

            GameLoginRequest loginRequest = buildLoginRequest(form, privateRoomSettings);
            LOG.debug("BattlegroundStartPrivateGameAction process: loginRequest: {}", loginRequest);

            LoginResponse login = LoginHelper.CWv3.getHelper().login(loginRequest);

            AccountInfo account = login.getAccountInfo();
            String externalId = account != null ? account.getExternalId() : null;

            SessionInfo sessionInfo = login.getSessionInfo();
            String sessionId = sessionInfo != null ? sessionInfo.getSessionId() : null;
            String privateRoomId = sessionInfo != null ? sessionInfo.getPrivateRoomId() : null;

            int gameId = privateRoomSettings.getGameId();

            String lang = form.getLang();
            if (StringUtils.isTrimmedEmpty(lang)) {
                lang = LanguageDetector.getAlternateLanguage(form.getBankInfo(), gameId, account);
            }

            if (login.getNotFinishedGameId() != null) {

                String homeUrl = form.getHomeUrl();
                if (StringUtils.isTrimmedEmpty(homeUrl)) {
                    String bgHomeUrlHost = bankInfo.getHomeUrlHost();
                    String buyInSelectUrl = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).getBuyInSelectUrl();
                    homeUrl = bgHomeUrlHost + buyInSelectUrl;
                }

                return redirectTIIncompleteRoundPage(bankInfo, login.getNotFinishedGameId(),
                        privateRoomSettings.getBuyIn(), lang, homeUrl, privateRoomId, request);
            }

            ActionRedirect mpRedirect = this.getMultiPlayerForward(form, request, GameMode.REAL, bankInfo, sessionId, lang, gameId);

            mpRedirect.addParameter("privateRoomId", form.getPrivateRoomId());
            mpRedirect.addParameter("battlegroundBuyIn", privateRoomSettings.getBuyIn());
            mpRedirect.addParameter("continueIncompleteRound", request.getParameter("continueIncompleteRound"));

            getLog().debug("BattlegroundStartPrivateGameAction process: SID={}, ExternalId={}, PrivateRoomId={}, " +
                    "isFinishGameSession=false", sessionId, externalId, privateRoomId);

            savePlayerSessionState(sessionId, externalId, privateRoomId, false, System.currentTimeMillis());

            LOG.debug("BattlegroundStartPrivateGameAction process: SID={} mpRedirect:{}", sessionId, mpRedirect);
            return mpRedirect;

        } catch (MaintenanceModeException e) {
            getLog().warn(e.getMessage());
            request.setAttribute(StartParameters.class.getSimpleName(), e.getParameters());
            return new ActionForward(gameServerConfiguration.getMaintenancePage());
        } catch (LoginErrorException e) {
            LOG.error("BattlegroundStartPrivateGameAction process: Could not login. form={}", form, e);
            addErrorWithPersistence(request, "error.login.internalError", e, System.currentTimeMillis());
            return mapping.findForward(ERROR_FORWARD);
        } catch (Exception e) {
            getLog().error("BattlegroundStartPrivateGameAction process: error", e);
            return mapping.findForward(ERROR_FORWARD);
        }
    }

    private GameLoginRequest buildLoginRequest(BattlegroundStartPrivateGameForm form, BattlegroundPrivateRoomSetting privateRoomSettings) {
        GameLoginRequest loginRequest = new GameLoginRequest();
        loginRequest.setBankId((int) privateRoomSettings.getBankId());
        loginRequest.setGameId(privateRoomSettings.getGameId());
        loginRequest.setToken("MMC".equalsIgnoreCase(privateRoomSettings.getCurrency()) ? form.getMmcToken() : form.getMqcToken());
        loginRequest.setGameMode(GameMode.getByName(form.getMode()));
        loginRequest.setSubCasinoId(form.getSubCasinoId());
        loginRequest.setRemoteHost(form.getHost());
        loginRequest.setClientType(form.getClientType());
        loginRequest.setPrivateRoomId(form.getPrivateRoomId());
        return loginRequest;
    }
}
