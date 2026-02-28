package com.abs.casino.gs;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.MaintenanceModeException;
import com.abs.casino.common.util.ILongIdGenerator;
import com.abs.casino.gs.managers.dblink.IDBLink;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 13.01.2020
 */
public interface IGameServer {

    Long startGame(SessionInfo sessionInfo, IBaseGameInfo gameInfo,
                   GameMode mode, Long bonusId, String lang, AccountInfo accountInfo) throws CommonException;

    Long startGame(SessionInfo sessionInfo, IBaseGameInfo gameInfo,
                   Long gameSessionId, GameMode mode, Long bonusId,
                   String lang, AccountInfo accountInfo) throws CommonException;

    IDBLink restartGame(SessionInfo sessionInfo, GameSession gameSession) throws CommonException;

    void closeOnlineGame(long gameSessionId, boolean limitsChanged,
                         long serverId, SessionInfo sessionInfo, boolean sitOut) throws CommonException;

    void closeOnlineGame(AccountInfo accountInfo, SessionInfo sessionInfo, GameSession gameSession,
                         boolean limitsChanged, boolean sitOut) throws CommonException;

    boolean needCloseMultiplayerGame(GameSession oldGameSession, BankInfo bankInfo, long newGameId);

    boolean isMultiplayerGame(GameSession gameSession);

    void checkMaintenanceMode(GameMode mode, String lang, AccountInfo accountInfo,
                              long gameId) throws MaintenanceModeException;

    ILongIdGenerator getIdGenerator();

    int getServerId();

    String getHost();

    ServerInfo getServerInfo();

    boolean isInitialized();
}
