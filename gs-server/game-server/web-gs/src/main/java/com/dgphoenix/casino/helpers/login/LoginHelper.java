package com.dgphoenix.casino.helpers.login;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.abs.casino.entities.AuthRequest;
import com.abs.casino.exceptions.LoginErrorException;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletProtocolManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.abs.casino.gs.managers.payment.wallet.v3.CommonWalletAuthResult;
import com.abs.casino.gs.managers.payment.wallet.v3.ICommonWalletClient;
import com.dgphoenix.casino.gs.persistance.remotecall.KafkaRequestMultiPlayer;
import com.dgphoenix.casino.kafka.dto.SitOutRequest2;
import com.dgphoenix.casino.sm.IPlayerSessionManager;
import com.dgphoenix.casino.sm.PlayerSessionFactory;
import com.abs.casino.sm.login.GameLoginRequest;
import com.abs.casino.sm.login.LoginRequest;
import com.abs.casino.sm.login.LoginResponse;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User: isirbis
 * Date: 03.10.14
 */
public abstract class LoginHelper<R extends AuthRequest, L extends LoginRequest> {
    private static final Logger LOG = LogManager.getLogger(LoginHelper.class);

    public static final String PROPERTY_GAMEMODE = "gameMode";
    public static final String PROPERTY_BANK_ID = "bankId";
    public static final String PROPERTY_GAME_ID = "gameId";
    public static final String PROPERTY_SERVER_ID = "serverId";

    protected static final GameServerConfiguration serverConfiguration = ApplicationContextHelper.getBean(GameServerConfiguration.class);
    protected static final KafkaRequestMultiPlayer kafkaRequestMultiPlayer = ApplicationContextHelper.getBean(KafkaRequestMultiPlayer.class);

    public static Map<String, String> getRequestAuthProperties(HttpServletRequest request) {
        Map<String, String> properties = new HashMap<>();

        String gameId = BaseAction.extractRequestParameterIgnoreCase(request, PROPERTY_GAME_ID);
        if (!StringUtils.isTrimmedEmpty(gameId)) {
            properties.put(PROPERTY_GAME_ID, gameId);
        }

        String serverId = BaseAction.extractRequestParameterIgnoreCase(request, PROPERTY_SERVER_ID);
        if (!StringUtils.isTrimmedEmpty(gameId)) {
            properties.put(PROPERTY_SERVER_ID, serverId);
        }

        return properties;
    }

    public static void performMaxQuestSitOut(AccountInfo accountInfo, GameSession gameSession, BankInfo bankInfo) throws CommonException {
        kafkaRequestMultiPlayer.sitOut(new SitOutRequest2(accountInfo.getId(), gameSession.getId()));
    }

    protected Logger getLogger() {
        return LOG;
    }

    public abstract LoginResponse login(L loginRequest) throws LoginErrorException;

    protected CommonWalletAuthResult getAuthInfo(R authRequest, GameLoginRequest loginRequest, BankInfo bankInfo) throws CommonException {
        String token = authRequest.getToken();
        GameMode gameMode = authRequest.getGameMode();
        String remoteHost = authRequest.getRemoteHost();
        ClientType clientType = authRequest.getClientType();

        short subCasinoId = (short) bankInfo.getSubCasinoId();
        int bankId = (int) bankInfo.getId();

        if (StringUtils.isTrimmedEmpty(token) && gameMode.equals(GameMode.REAL)) {
            throw new CommonException("incorrect parameters: empty token and real mode");
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("login: token = {}, bankId = {}, subCasinoId = {}, host = {}, mode = {}",
                    token, bankId, subCasinoId, remoteHost, gameMode);
        }

        IWalletProtocolManager ocwm = WalletProtocolFactory.getInstance().
                getWalletProtocolManager(bankId);
        ICommonWalletClient client = (ICommonWalletClient) ocwm.getClient();
        CommonWalletAuthResult authResult;
        if (isNeedSendGameIdOnAuth(subCasinoId, loginRequest)) {
            String gameId = authRequest.getProperties().get(PROPERTY_GAME_ID);
            if (StringUtils.isTrimmedEmpty(gameId)) {
                gameId = String.valueOf(loginRequest.getGameId());
            }
            authResult = client.auth(token, gameId, clientType);
        } else if (subCasinoId == 44) {
            //GSN
            String serverId = authRequest.getProperties().get(PROPERTY_SERVER_ID);
            authResult = client.auth(token, null, serverId, clientType);
        } else if (authRequest.getProperties() != null && !authRequest.getProperties().isEmpty()) {
            authResult = client.auth(token, null, null, clientType, authRequest.getProperties());
        } else {
            authResult = client.auth(token, clientType);
        }
        return authResult;
    }

    private boolean isNeedSendGameIdOnAuth(short subCasinoId, GameLoginRequest loginRequest) {
        // 0002807: VERA JOHN -- INTEGRATION AND SUPPORT
        // 0002573: X18
        return subCasinoId == 39 || subCasinoId == 62 || subCasinoId == 212 || loginRequest.isNeedSendGameIdOnAuth();
    }

    protected SessionInfo commonGuestLogin(String randomStr, short subCasinoId, int bankId, String remoteHost,
                                           ClientType clientType, String fakeExternalSessionId)
            throws CommonException {

        if (!SessionHelper.getInstance().isTransactionStarted()) {
            throw new CommonException("Transaction not started");
        }

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        String nickName = (bankInfo.getExternalBankIdDescription() + "_" + randomStr).substring(0, 20);
        Currency defaultCurrency = bankInfo.getDefaultCurrency();

        AccountInfo accountInfo = AccountManager.getInstance().saveAccount(null, randomStr, bankInfo,
                subCasinoId, nickName, true, false, null, clientType, null, null, defaultCurrency, null, true);

        IPlayerSessionManager psm = PlayerSessionFactory.getInstance().getPlayerSessionManager(bankId);

        //when not login from stlobby
        if (fakeExternalSessionId == null) {
            fakeExternalSessionId = StringIdGenerator.generateSessionId(GameServer.getInstance().getServerId(),
                    accountInfo.getBankId(), accountInfo.getExternalId());
        }
        return psm.login(accountInfo, fakeExternalSessionId, remoteHost, clientType);
    }

    public static boolean needRedirectToIncompleteActiveRound(long currentGameId, GameSession oldGameSession) {

        LOG.debug("needRedirectToIncompleteActiveRound: currentGameId={}, oldGameSession={}", currentGameId, oldGameSession);

        if (oldGameSession == null) {
            return false;
        }

        long oldGameId = oldGameSession.getGameId();
        if (currentGameId == oldGameId) {
            return false;
        }

        BaseGameInfoTemplate oldGameInfoTemplateById = BaseGameInfoTemplateCache.getInstance()
                .getBaseGameInfoTemplateById(oldGameId);

        boolean isBtgOldGame = oldGameInfoTemplateById.isBattleGroundsMultiplayerGame();

        LOG.debug("needRedirectToIncompleteActiveRound: oldGameId={}, isBtgOldGame={}, oldGameInfoTemplateById={}",
                oldGameId, isBtgOldGame, oldGameInfoTemplateById);

        return isBtgOldGame || Arrays.asList(864L, 875L).contains(oldGameId); //BG_MAXCRASHGAME=864, TRIPLE_MAX_BLAST=875
    }

}
