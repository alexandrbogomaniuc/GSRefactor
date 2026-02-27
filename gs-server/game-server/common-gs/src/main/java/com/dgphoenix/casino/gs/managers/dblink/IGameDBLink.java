package com.abs.casino.gs.managers.dblink;

import com.dgphoenix.casino.common.cache.data.account.IPlayerGameSettings;
import com.dgphoenix.casino.common.cache.data.bank.ICoin;
import com.dgphoenix.casino.common.cache.data.bank.ILimit;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.gs.singlegames.tools.cbservtools.CBGameException;
import com.abs.casino.unj.api.AbstractSharedGameState;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 12.04.2023.
 */
public interface IGameDBLink {
    long getAccountId();

    int getActiveGameID();

    long getBankId();

    boolean isUsePlayerGameSettings();

    IPlayerGameSettings getPlayerGameSettings();

    long getGameId();

    String getGameName();

    Long getRoomId();

    ICurrency getCurrency();

    ICurrency getActiveCurrency();

    boolean isWalletBank();

    long getGameSessionId();

    String getNickName();

    Long getRoundId();

    void setRoundId(Long roundId);

    long getWinAmount();

    void setWinAmount(long winAmount);

    long getCurrentBet();

    long getCurrentWin();

    boolean isForReal();

    double getBalance();

    boolean isLimitsChanged();

    void setLimitsChanged(boolean limitsChanged);

    Map<String, String> getLastHandLimits();

    void setLastHandLimit(String key, String value);

    void resetLasthandLimits();

    void setLasthand(String data) throws CommonException;

    String getLasthand() throws CommonException;

    boolean getLastHand(Map publicLastHand, Map privateLastHand, Map autoPublic,
                        Map autoPrivate) throws CBGameException;

    boolean getLastHand(int gameID, Map publicLastHand, Map privateLastHand, Map autoPublic,
                        Map autoPrivate) throws CBGameException;

    String getLasthand(long gameID);

    boolean isLocked();

    void setRoundFinished() throws CommonException;

    boolean isFRBGame();

    void incrementBalance(long bet, long win) throws CommonException;

    Double getMinBet();

    Double getConvertedMinBet();

    void setMinBet(Double minBet);

    Double getMaxBet();

    Double getConvertedMaxBet();

    double getPayoutPercent();

    void setMaxBet(Double maxBet);

    double[] getCOINSEQ();

    double[] getConvertedCOINSEQ();

    void setCOINSEQ(double[] COINSEQ);

    boolean isBonusGameSession();

    String getDefaultBetPerLineNotFRB();

    String getDefaultNumLinesNotFRB();

    String getCurrentDefaultBetPerLine();

    String getCurrentDefaultNumLines();

    String getFRBDefaultBetPerLine();

    String getFRBDefaultNumLines();

    String getFRBCoin();

    Integer getDefaultCoin();

    String getChipValues();

    List<? extends ICoin> getCoins();

    List<? extends ICoin> getCoins(boolean withCache);

    boolean isUseDynamicLevels();

    ILimit getLimit();

    String getGameSettingsProperty(String key);

    int getSubCasinoId();

    boolean isMaxBet(long betsCount);

    AbstractSharedGameState getSharedGameState(String unjExtraId)  throws CommonException;

    void updateSharedGameState(String unjExtraId, AbstractSharedGameState state)  throws CommonException;

    boolean isGameLogEnabled();

    void setRequestParameters(Map<String, String[]> parameters);

    Map<String, String[]> getRequestParameters();

    String getRequestParameterValue(String parameterName);

}
