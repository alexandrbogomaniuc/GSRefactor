package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.web.ISocketClient;

import java.util.List;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ILobbySession {
    String getSessionId();

    void setSessionId(String sessionId);

    long getAccountId();

    void setAccountId(long accountId);

    long getBankId();

    void setBankId(long bankId);

    String getNickname();

    void setNickname(String nickname);

    boolean isNicknameEditable();

    void setNicknameEditable(boolean nicknameEditable);

    IAvatar getAvatar();

    void setAvatar(IAvatar avatar);

    long getEnterDate();

    void setEnterDate(long enterDate);

    long getBalance();

    void setBalance(long balance);

    long getRoomId();

    void setRoomId(long roomId);

    ISocketClient getSocketClient();

    void setSocketClient(ISocketClient socketClient);

    boolean isDisableTooltips();

    void setDisableTooltips(boolean disableTooltips);

    String getWebsocketSessionId();

    void setWebsocketSessionId(String websocketSessionId);

    ICurrency getCurrency();

    void setCurrency(ICurrency currency);

    boolean isShowRefreshButton();

    void setShowRefreshButton(boolean showRefreshButton);

    List<Long> getStakes();

    void setStakes(List<Long> stakes);

    int getStakesReserve();

    void setStakesReserve(int stakesReserve);

    int getStakesLimit();

    void setStakesLimit(int stakesLimit);

    double getLbContributionPercent();

    void setLbContributionPercent(double lbContributionPercent);

    boolean isLeaderboardDisabled();

    long getGameId();

    void setGameId(long gameId);

    MaxQuestWeaponMode getWeaponMode();

    void setWeaponMode(MaxQuestWeaponMode weaponMode);

    boolean isAllowWeaponSaveInAllGames();

    void setAllowWeaponSaveInAllGames(boolean allowWeaponSaveInAllGames);

    MoneyType getMoneyType();

    void setMoneyType(MoneyType moneyType);

    IActiveCashBonusSession getActiveCashBonusSession();

    void setActiveCashBonusSession(IActiveCashBonusSession session);

    ITournamentSession getTournamentSession();

    void setTournamentSession(ITournamentSession tournamentSession);

    IActiveFrbSession getActiveFrbSession();

    void setActiveFrbSession(IActiveFrbSession activeFrbSession);

    boolean isConfirmBattlegroundBuyIn();

    void setConfirmBattlegroundBuyIn(boolean confirmBattlegroundBuyIn);

    boolean isBattlegroundAllowed();

    void setBattlegroundAllowed(boolean battlegroundAllowed);

    List<Long> getBattlegroundBuyIns();

    void setBattlegroundBuyIns(List<Long> battlegroundBuyIns);

    double getBattlegroundRakePercent();

    void setBattlegroundRakePercent(double battlegroundRakePercent);

    boolean isPrivateRoom();

    void setPrivateRoom(boolean privateRoom);

    boolean isOwner();

    void setOwner(boolean owner);

    boolean isSendRealBetWin();

    void setSendRealBetWin(boolean sendRealBetWin);

    void setExternalId(String externalId);

    String getExternalId();
}
