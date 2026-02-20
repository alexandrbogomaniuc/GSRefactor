package com.dgphoenix.casino.gs.managers.dblink;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.gs.persistance.LasthandPersister;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.dgphoenix.casino.unj.api.AbstractSharedGameState;
import com.dgphoenix.casino.unj.api.SharedGameStates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DBLinkFree extends DBLink {
    private static final Logger LOG = LogManager.getLogger(DBLinkFree.class);

    private Map<String, SharedGameStates> sharedGameStates;
    private final GameServerConfiguration configuration;

    public DBLinkFree(long accountId, String nickName, long cafeId, long gameId, String gameName, boolean isJackpotGame,
                      GameSession gameSession, Currency currency) {
        super(accountId, nickName, cafeId, gameId, gameName, isJackpotGame, gameSession, currency);
        configuration = ApplicationContextHelper.getBean(GameServerConfiguration.class);
        LOG.debug("recreate DBLinkFree: {}", accountId);
    }

    public DBLinkFree(AccountInfo accountInfo, long gameId, Long gameSessionId, SessionInfo sessionInfo,
                      IBaseGameInfo gameInfo, String lang) throws CommonException {
        super(accountInfo, gameId, gameSessionId, sessionInfo, gameInfo, lang);
        configuration = ApplicationContextHelper.getBean(GameServerConfiguration.class);
        FreeGameCalculator freeGameCalculator = ApplicationContextHelper.getBean(FreeGameCalculator.class);
        accountInfo.setFreeBalance(freeGameCalculator.calculateFreeBalance(gameInfo, accountInfo.getCurrency().getCode()));
        LOG.debug("create DBLinkFree: {}", accountId);
    }

    @Override
    public boolean isForReal() {
        return false;
    }

    @Override
    public GameMode getMode() {
        return GameMode.FREE;
    }

    @Override
    public long getBalanceLong() {
        return getAccount().getFreeBalance();
    }

    @Override
    public void incrementBalance(long bet, long win) {
        if (LOG.isDebugEnabled()) {
            logDebug("incrementBalance amount:" + (bet + win));
        }

        AccountInfo accountInfo = getAccount();
        accountInfo.setFreeBalance(accountInfo.getFreeBalance() + bet + win);
    }

    @Override
    public GameSession finishGameSession(GameSession gameSession, SessionInfo sessionInfo) {
        LasthandPersister.getInstance().clearCached();
        logDebug("finishGameSession FREE gameSessionId:" + gameSession.getId() + " finished OK");
        return gameSession;
    }

    private String composeKey(long accountId, long gameId) {
        return accountId + "+" + gameId;
    }

    @Override
    public String getLogPrefix() {
        return "DBLinkFree [accountId=" + accountId + ", nickName=" + nickName + ", gameId=" + gameId
                + ", gameSessionId=" + gameSessionId + "] ";
    }

    @Override
    public IWallet getWallet() {
        return null;
    }

    @Override
    public AbstractSharedGameState getSharedGameState(String unjExtraId) throws CommonException {
        if (sharedGameStates == null) {
            sharedGameStates = new HashMap<>();
        }
        SharedGameStates sharedStates = sharedGameStates.computeIfAbsent(unjExtraId, v -> new SharedGameStates());
        AbstractSharedGameState<?, ?> sharedGameState = sharedStates.get(gameId);
        if (sharedGameState == null) {
            //unj code removed
        }
        return sharedGameState;
    }

    @Override
    public void updateSharedGameState(String unjExtraId, AbstractSharedGameState state) {
        //nop
    }
}
