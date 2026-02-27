package com.abs.casino.actions.game.battleground;

import com.abs.casino.actions.enter.LanguageDetector;
import com.dgphoenix.casino.actions.enter.game.BaseStartGameAction;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.MaintenanceModeException;
import com.dgphoenix.casino.common.exception.StartParameters;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.entities.lobby.LoginHelper;
import com.abs.casino.exceptions.LoginErrorException;
import com.dgphoenix.casino.sm.login.GameLoginRequest;
import com.dgphoenix.casino.sm.login.LoginResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BattlegroundStartGameFromMQBAction extends BaseStartGameAction<BattlegroundStartGameFromMQBForm> {
    private static final Logger LOG = LogManager.getLogger(BattlegroundStartGameFromMQBAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, BattlegroundStartGameFromMQBForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {

        getLog().debug("BattlegroundStartGameFromMQBAction process: enter process mapping={}, form={}, request={}", mapping, form, request);

        try {
            long bankId = form.getBankId();
            validateMpPass(request, (int) bankId);
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null) {
                throw new CommonException("BankInfo is null for bankId: " + bankId);
            }

            GameLoginRequest loginRequest = buildLoginRequest(form, request, bankInfo);
            LOG.debug("BattlegroundStartGameFromMQBAction process: build loginRequest: {}", loginRequest);

            LoginResponse loginResponse = LoginHelper.CWv3.getHelper().login(loginRequest);
            LOG.debug("BattlegroundStartGameFromMQBAction process: loginResponse: {}", loginResponse);

            AccountInfo account = loginResponse.getAccountInfo();
            String externalId = account != null ? account.getExternalId() : null;

            SessionInfo sessionInfo = loginResponse.getSessionInfo();
            String sessionId = sessionInfo != null ? sessionInfo.getSessionId() : null;
            String privateRoomId = sessionInfo != null ? sessionInfo.getPrivateRoomId() : null;

            int gameId = Integer.parseInt(form.getGameId());

            String lang = form.getLang();
            if (StringUtils.isTrimmedEmpty(lang)) {
                lang = LanguageDetector.getAlternateLanguage(bankInfo, gameId, account);
            }

            if (loginResponse.getNotFinishedGameId() != null) {

                LOG.debug("BattlegroundStartGameFromMQBAction process: there is a Not Finished GameId: {}",
                        loginResponse.getNotFinishedGameId());

                String homeUrl = form.getHomeUrl();
                if (StringUtils.isTrimmedEmpty(homeUrl)) {
                    String bgHomeUrlHost = bankInfo.getHomeUrlHost();
                    String buyInSelectUrl = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).getBuyInSelectUrl();
                    homeUrl = bgHomeUrlHost + buyInSelectUrl;
                }

                LOG.debug("BattlegroundStartGameFromMQBAction process: SID={} redirectTIIncompleteRoundPage: " +
                                "loginResponse.getNotFinishedGameId()={}, form.getBuyIn()={}, lang={}, homeUrl={}, privateRoomId={}, request={}",
                        sessionId, loginResponse.getNotFinishedGameId(), form.getBuyIn(), lang, homeUrl, privateRoomId, request);

                return redirectTIIncompleteRoundPage(bankInfo, loginResponse.getNotFinishedGameId(), form.getBuyIn(),
                        lang, homeUrl, privateRoomId, request);
            }

            ActionRedirect mpRedirect = getMultiPlayerForward(form, request, GameMode.REAL, bankInfo, sessionId, lang, gameId);

            mpRedirect.addParameter("battlegroundBuyIn", form.getBuyIn());
            if(!StringUtils.isTrimmedEmpty(form.getPrefRoomId())) {
                mpRedirect.addParameter("prefRoomId", form.getPrefRoomId());
            }
            mpRedirect.addParameter("continueIncompleteRound", request.getParameter("continueIncompleteRound"));

            getLog().debug("BattlegroundStartGameFromMQBAction process: SID={}, ExternalId={}, PrivateRoomId={}, " +
                            "isFinishGameSession=false", sessionId, externalId, privateRoomId);

            savePlayerSessionState(sessionId, externalId, privateRoomId, false, System.currentTimeMillis());

            LOG.debug("BattlegroundStartGameFromMQBAction process: SID={} mpRedirect:{}", sessionId, mpRedirect);
            return mpRedirect;

        } catch (MaintenanceModeException e) {
            getLog().warn(e.getMessage());
            request.setAttribute(StartParameters.class.getSimpleName(), e.getParameters());
            return new ActionForward(gameServerConfiguration.getMaintenancePage());
        } catch (LoginErrorException e) {
            LOG.error("Could not login. form={}", form, e);
            addErrorWithPersistence(request, "error.login.internalError", e, System.currentTimeMillis());
            return mapping.findForward(ERROR_FORWARD);
        } catch (Exception e) {
            getLog().error("process error", e);
            return mapping.findForward(ERROR_FORWARD);
        }
    }

    private GameLoginRequest buildLoginRequest(BattlegroundStartGameFromMQBForm form, HttpServletRequest request, BankInfo bankInfo) {
        GameLoginRequest loginRequest = new GameLoginRequest();
        loginRequest.setBankId(form.getBankId());
        loginRequest.setGameId(Integer.parseInt(form.getGameId()));
        loginRequest.setNeedSendGameIdOnAuth(bankInfo.isSendGameIdOnAuth());
        loginRequest.setToken(form.getToken());
        loginRequest.setGameMode(GameMode.getByName(form.getMode()));
        loginRequest.setSubCasinoId(form.getSubCasinoId());
        loginRequest.setRemoteHost(request.getRemoteHost());
        loginRequest.setClientType(form.getClientType());
        return loginRequest;
    }

    @Override
    protected void persistError(HttpServletRequest request, Exception exception, long exceptionTime) {
        errorPersisterHelper.persistStartGameActionError(request, exception, exceptionTime);
    }

}
