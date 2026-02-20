package com.dgphoenix.casino.gs.managers.dblink;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.DBException;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletDBLink;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.CBGameException;

/**
 * Created
 * Date: 24.11.2008
 * Time: 15:42:22
 */
public interface IDBLink extends IGameDBLink, IWalletDBLink {

    AccountInfo getAccount();

    void markEnterGame(int gameID, String gameName, int gamestate) throws CBGameException;

    void setLastSavedBet(PlayerBet bet);

    String getLogPrefix();

    void setLastPaymentOperationId(Long lastPaymentOperationId);

    Currency getCurrency();

    GameSession getGameSession();

    GameMode getMode();

    void resetCurrentBetWin();

    void updateCurrentBetWin(long betAmount, long winAmount);

    GameSession finishGameSession(GameSession gameSession, SessionInfo sessionInfo) throws CommonException;

    void refreshGameSettings();

    IBaseGameInfo getGameSettings();

    double getDBBonus() throws CBGameException;

    long getBalanceLong();

    void saveLasthand(String data) throws DBException;

    LasthandInfo getLasthandInfo(long gameID);

    boolean isAuthState();

    void setAuthState(boolean authState);

    IWallet getWallet();

    void saveWinAmount(long winAmount);

    void interceptBet(long bet, long win) throws CommonException;

    void interceptRoundFinished();

    SessionInfo getSessionInfo();

    long getLastActivity();

    void updateLastActivity();

    boolean isNeedUpdateLastActivity();

    boolean isSendRoundId();

    boolean isSendExternalWalletMessages();

    boolean isCloseOldGameAfterRoundFinished();

    int getTimeZoneOffset() throws CommonException;

    boolean isSaveGameSidByRound();

    boolean isSaveShortBetInfo();

    Long getLastCheckTime();

    void setLastCheckTime(Long lastCheckTime);

    boolean isNotFixAnyChanges();

    long generateRoundId();

    void saveGameSessionRealityCheckParams();

    boolean isBonusInstantLostOnThreshold();

    double adjustMoneyValue(double originalValue);

    boolean isLogoutOnError();

    Long getWalletTransactionId();
}
