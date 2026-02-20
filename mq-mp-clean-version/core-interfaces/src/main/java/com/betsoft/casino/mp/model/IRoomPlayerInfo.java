package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.common.cache.Identifiable;

import java.util.Map;

/**
 * User: flsh
 * Date: 18.05.2020.
 */
public interface IRoomPlayerInfo extends Identifiable {

    void setId(long id);

    /**
     * Get player session id of player
     * @return player session id
     */
    String getSessionId();

    /**
     * Set player sessionId of player
     * @param sessionId player sessionId of player
     */
    void setSessionId(String sessionId);

    long getBankId();

    void setBankId(long bankId);

    long getRoomId();

    void setRoomId(long roomId);

    int getSeatNumber();

    void setSeatNumber(int seatNumber);

    long getGameSessionId();

    void setGameSessionId(long gameSessionId);

    String getNickname();

    void setNickname(String nickname);

    long getEnterDate();

    void setEnterDate(long enterDate);

    IExperience getTotalScore();

    IExperience getCurrentScore();

    ICurrency getCurrency();

    void setCurrency(ICurrency currency);

    boolean isWantSitOut();

    void setWantSitOut(boolean wantSitOut);

    IAvatar getAvatar();

    void setAvatar(IAvatar avatar);

    /**
     * Checks if the player has pending operations. During this time, other transactions are prohibited.
     * @return boolean  true if player has pending operations
     */
    boolean isPendingOperation();

    void setPendingOperation(boolean pendingOperation);

    void setPendingOperation(boolean pendingOperation, String lastOperationInfo);

    String getLastOperationInfo();

    void setLastOperationInfo(String lastOperationInfo);

    IPlayerStats getStats();

    void setStats(IPlayerStats stats);

    IPlayerStats setNewPlayerStats();

    IPlayerStats getRoundStats();

    void setRoundStats(IPlayerStats roundStats);

    void setNewRoundStats();

    boolean isShowRefreshButton();

    void setShowRefreshButton(boolean showRefreshButton);

    long getExternalRoundId();

    void setExternalRoundId(long externalRoundId);

    long getRoundBuyInAmount();

    void setRoundBuyInAmount(long roundBuyInAmount);

    IPlayerQuests getPlayerQuests();

    void setPlayerQuests(IPlayerQuests playerQuests);

    IExperience getPrevXP();

    void setPrevXP(IExperience prevXP);

    void finishCurrentRound();

    /**
     * Update externalRoundId from last operation and increase total amount of buyIn in round
     * @param externalRoundId externalRoundId of round
     * @param roundBuyInAmount last amount buyIn
     */
    void makeBuyIn(long externalRoundId, long roundBuyInAmount);

    long getStake();

    void setStake(long stake);

    int getStakesReserve();

    void setStakesReserve(int stakesReserve);

    int getBuyInCount();

    void setBuyInCount(int buyInCount);

    void incrementBuyInCount();

    IPlayerBet createNewPlayerBet();

    String toShortString();

    IActiveFrbSession getActiveFrbSession();

    void setActiveFrbSession(IActiveFrbSession activeFrbSession);

    IActiveCashBonusSession getActiveCashBonusSession();

    void setActiveCashBonusSession(IActiveCashBonusSession activeCashBonusSession);

    ITournamentSession getTournamentSession();

    void setTournamentSession(ITournamentSession tournamentSession);
}
