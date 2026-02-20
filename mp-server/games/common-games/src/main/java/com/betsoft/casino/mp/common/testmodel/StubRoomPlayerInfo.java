package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.battleground.IBgPlace;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StubRoomPlayerInfo implements ICrashGameRoomPlayerInfo, IBattlegroundRoomPlayerInfo {

    private long id;
    private long bankId;
    private long roomId;
    private int seatNumber;
    private String sessionId;
    private long gameSessionId;
    private String nickname;
    private long enterDate;
    private ICurrency currency;
    private boolean wantSitOut;
    private IAvatar avatar;
    private int specialWeaponId;
    private boolean showRefreshButton;
    private long externalRoundId;
    private long roundBuyInAmount;
    private IExperience prevXP = new Experience(0);
    private boolean pendingOperation = false;
    private String lastOperationInfo;
    private IPlayerStats stats;
    private IPlayerStats roundStats;
    private Map<Integer, Integer> weapons = new HashMap<>();
    private IPlayerQuests playerQuests;
    //stake in cents and playerCurrency
    private long stake;
    //number of stakes to be transferred from balance
    private int stakesReserve;
    private double lbContributionPercent;
    private int buyInCount = 0;
    private MaxQuestWeaponMode weaponMode;
    private boolean allowWeaponSaveInAllGames;
    private boolean battlegroundMode;
    private double battleGroundRake;
    private boolean privateRoom;
    private boolean isOwner;

    public StubRoomPlayerInfo() {
    }

    public StubRoomPlayerInfo(long id, long bankId, long roomId, int seatNumber, String sessionId, long gameSessionId,
                              String nickname, IAvatar avatar, long enterDate, ICurrency currency,
                              IPlayerStats stats, boolean showRefreshButton, Map<Integer, Integer> weapons,
                              IPlayerQuests playerQuests, long stake, int stakesReserve,
                              double lbContributionPercent, MaxQuestWeaponMode weaponMode, boolean allowWeaponSaveInAllGames) {
        this.id = id;
        this.bankId = bankId;
        this.roomId = roomId;
        this.seatNumber = seatNumber;
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.enterDate = enterDate;
        this.currency = currency;
        this.specialWeaponId = -1;
        this.stats = stats;
        this.showRefreshButton = showRefreshButton;
        if (weapons != null) {
            this.weapons = weapons;
        }
        roundStats = new StubPlayerStats();
        this.stake = stake;
        this.stakesReserve = stakesReserve;
        this.lbContributionPercent = lbContributionPercent;
        this.weaponMode = weaponMode;
        this.allowWeaponSaveInAllGames = allowWeaponSaveInAllGames;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public int getSeatNumber() {
        return seatNumber;
    }

    @Override
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public long getGameSessionId() {
        return gameSessionId;
    }

    @Override
    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public long getEnterDate() {
        return enterDate;
    }

    @Override
    public void setEnterDate(long enterDate) {
        this.enterDate = enterDate;
    }

    @Override
    public IExperience getTotalScore() {
        return stats.getScore();
    }

    @Override
    public IExperience getCurrentScore() {
        return roundStats.getScore();
    }

    public ICurrency getCurrency() {
        return currency;
    }

    @Override
    public void setCurrency(ICurrency currency) {
        this.currency = currency;
    }

    @Override
    public boolean isWantSitOut() {
        return wantSitOut;
    }

    @Override
    public void setWantSitOut(boolean wantSitOut) {
        this.wantSitOut = wantSitOut;
    }

    @Override
    public IAvatar getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(IAvatar avatar) {
        this.avatar = avatar;
    }

    @Override
    public int getSpecialWeaponId() {
        return specialWeaponId;
    }

    @Override
    public void setSpecialWeaponId(int specialWeaponId) {
        this.specialWeaponId = specialWeaponId;
    }

    @Override
    public boolean isPendingOperation() {
        return pendingOperation;
    }

    @Override
    public void setPendingOperation(boolean pendingOperation) {
        this.pendingOperation = pendingOperation;
    }

    @Override
    public void setPendingOperation(boolean pendingOperation, String lastOperationInfo) {
        this.pendingOperation = pendingOperation;
        this.lastOperationInfo = lastOperationInfo;
    }

    @Override
    public boolean isAllowWeaponSaveInAllGames() {
        return allowWeaponSaveInAllGames;
    }

    @Override
    public void setAllowWeaponSaveInAllGames(boolean allowWeaponSaveInAllGames) {
        this.allowWeaponSaveInAllGames = allowWeaponSaveInAllGames;
    }

    @Override
    public String getLastOperationInfo() {
        return lastOperationInfo;
    }

    @Override
    public void setLastOperationInfo(String lastOperationInfo) {
        this.lastOperationInfo = lastOperationInfo;
    }

    @Override
    public IPlayerStats getStats() {
        return stats;
    }

    @Override
    public void setStats(IPlayerStats stats) {
        this.stats = stats;
    }

    @Override
    public IPlayerStats setNewPlayerStats() {
        return this.stats;
    }

    @Override
    public IPlayerStats getRoundStats() {
        return roundStats;
    }

    @Override
    public void setRoundStats(IPlayerStats roundStats) {
        this.roundStats = roundStats;
    }

    @Override
    public void setNewRoundStats() {
    }

    @Override
    public boolean isShowRefreshButton() {
        return showRefreshButton;
    }

    @Override
    public void setShowRefreshButton(boolean showRefreshButton) {
        this.showRefreshButton = showRefreshButton;
    }

    @Override
    public long getExternalRoundId() {
        return externalRoundId;
    }

    @Override
    public void setExternalRoundId(long externalRoundId) {
        this.externalRoundId = externalRoundId;
    }

    @Override
    public long getRoundBuyInAmount() {
        return roundBuyInAmount;
    }

    @Override
    public void setRoundBuyInAmount(long roundBuyInAmount) {
        this.roundBuyInAmount = roundBuyInAmount;
    }

    @Override
    public IPlayerQuests getPlayerQuests() {
        return playerQuests;
    }

    @Override
    public void setPlayerQuests(IPlayerQuests playerQuests) {
        this.playerQuests = playerQuests;
    }

    @Override
    public IExperience getPrevXP() {
        return prevXP;
    }

    @Override
    public void setPrevXP(IExperience prevXP) {
        this.prevXP = prevXP;
    }

    @Override
    public void finishCurrentRound() {
        this.roundBuyInAmount = 0;
    }

    @Override
    public void makeBuyIn(long externalRoundId, long roundBuyInAmount) {
        this.externalRoundId = externalRoundId;
        this.roundBuyInAmount += roundBuyInAmount;
    }

    @Override
    public Map<Integer, Integer> getWeapons() {
        return weapons;
    }

    @Override
    public void setWeapons(Map<Integer, Integer> weapons) {
        if (weapons == null) {
            this.weapons.clear();
        } else {
            if (this.weapons == null) {
                this.weapons = new HashMap<>();
            } else {
                this.weapons.clear();
            }
            this.weapons.putAll(weapons);
        }
    }

    @Override
    public long getStake() {
        return stake;
    }

    @Override
    public void setStake(long stake) {
        this.stake = stake;
    }

    @Override
    public int getStakesReserve() {
        return stakesReserve;
    }

    @Override
    public void setStakesReserve(int stakesReserve) {
        this.stakesReserve = stakesReserve;
    }

    @Override
    public int getBuyInCount() {
        return buyInCount;
    }

    @Override
    public void setBuyInCount(int buyInCount) {
        this.buyInCount = buyInCount;
    }

    @Override
    public void incrementBuyInCount() {
        this.buyInCount++;
    }

    @Override
    public MaxQuestWeaponMode getWeaponMode() {
        return weaponMode == null ? MaxQuestWeaponMode.LOOT_BOX : weaponMode;
    }

    @Override
    public void setWeaponMode(MaxQuestWeaponMode weaponMode) {
        this.weaponMode = weaponMode;
    }

    @Override
    public IFreeShots createNewFreeShots() {
        return new StubFreeShots();
    }

    @Override
    public IMinePlace getNewMinePlace(long date, int rid, int seatId, float x, float y, String mineId) {
        return new StubMinePlace(date, rid, seatId, x, y, mineId);
    }

    @Override
    public IPlayerBet createNewPlayerBet() {
        return new StubPlayerBet(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubRoomPlayerInfo that = (StubRoomPlayerInfo) o;
        return id == that.id &&
                bankId == that.bankId &&
                gameSessionId == that.gameSessionId &&
                enterDate == that.enterDate &&
                wantSitOut == that.wantSitOut &&
                Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bankId);
    }

    public String toShortString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append("accountId=").append(id);
        sb.append(", roomId=").append(roomId);
        sb.append(", seatNumber=").append(seatNumber);
        sb.append(", sessionId=").append(sessionId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", wantSitOut=").append(wantSitOut);
        sb.append(", specialWeaponId=").append(specialWeaponId);
        sb.append(", externalRoundId=").append(externalRoundId);
        sb.append(", roundBuyInAmount=").append(roundBuyInAmount);
        sb.append(", buyInCount=").append(buyInCount);
        sb.append(", pendingOperation=").append(pendingOperation);
        sb.append(", stake=").append(stake);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public IActiveFrbSession getActiveFrbSession() {
        return null;
    }

    @Override
    public void setActiveFrbSession(IActiveFrbSession activeFrbSession) {

    }

    @Override
    public IActiveCashBonusSession getActiveCashBonusSession() {
        return null;
    }

    @Override
    public void setActiveCashBonusSession(IActiveCashBonusSession activeCashBonusSession) {

    }

    @Override
    public ITournamentSession getTournamentSession() {
        return null;
    }

    @Override
    public void setTournamentSession(ITournamentSession tournamentSession) {

    }

    @Override
    public IBattlegroundRoundInfo getBattlegroundRoundInfo() {
        return null;
    }

    @Override
    public void setBattlegroundRoundInfo(IBattlegroundRoundInfo battlegroundRoundInfo) {

    }

    @Override
    public IBgPlace createBattlegroundRoundInfo(long buyIn, long winAmount, long betsSum, long winSum, String status,
                                                int playersNumber, String winnerName, long accountId, int rank,
                                                long gameSessionId, long gameScore, long roundId, long roundStartDate, double ejectPoint, String privateRoomId) {
        return null;
    }

    @Override
    public double getBattlegroundRake() {
        return battleGroundRake;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    @Override
    public boolean isOwner() {
        return this.isOwner;
    }

    @Override
    public void setOwner(boolean roomOwner) {
        this.isOwner = roomOwner;
    }

    @Override
    public String getUserName() {
        return null;
    }

    @Override
    public void setUserName(String userName) {

    }

    @Override
    public void setBattlegroundRake(double battlegroundRake) {
        this.battleGroundRake = battlegroundRake;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StubRoomPlayerInfo [");
        sb.append("id=").append(id);
        sb.append(", bankId=").append(bankId);
        sb.append(", roomId=").append(roomId);
        sb.append(", seatNumber=").append(seatNumber);
        sb.append(", sessionId=").append(sessionId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", enterDate=").append(enterDate);
        sb.append(", currency=").append(currency);
        sb.append(", wantSitOut=").append(wantSitOut);
        sb.append(", avatar=").append(avatar);
        sb.append(", specialWeaponId=").append(specialWeaponId);
        sb.append(", showRefreshButton=").append(showRefreshButton);
        sb.append(", externalRoundId=").append(externalRoundId);
        sb.append(", roundBuyInAmount=").append(roundBuyInAmount);
        sb.append(", pendingOperation=").append(pendingOperation);
        sb.append(", stats=").append(stats);
        sb.append(", roundStats=").append(roundStats);
        sb.append(", weapons=").append(weapons);
        sb.append(", playerQuests=").append(playerQuests);
        sb.append(", prevXP=").append(prevXP);
        sb.append(", stake=").append(stake);
        sb.append(", stakesReserve=").append(stakesReserve);
        sb.append(", lbContributionPercent=").append(lbContributionPercent);
        sb.append(", lastOperationInfo='").append(lastOperationInfo).append("'");
        sb.append(", buyInCount=").append(buyInCount);
        sb.append(", battlegroundMode=").append(battlegroundMode);
        sb.append(", battleGroundRake=").append(battleGroundRake);
        sb.append(", privateRoom=").append(privateRoom);
        sb.append(", isOwner=").append(isOwner);
        sb.append(']');
        return sb.toString();
    }
}
