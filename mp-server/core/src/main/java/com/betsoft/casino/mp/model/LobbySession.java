package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.transport.Avatar;
import com.betsoft.casino.mp.transport.Currency;
import com.betsoft.casino.mp.web.ISocketClient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * User: flsh
 * Date: 08.05.18.
 */
public class LobbySession implements ILobbySession, KryoSerializable, Serializable {
    private static final byte VERSION = 3;
    private String sessionId;
    private long accountId;
    private long bankId;
    private String nickname;
    private boolean nicknameEditable = true;
    private IAvatar avatar;
    private long enterDate;
    private long balance;
    private long roomId;
    private boolean disableTooltips;
    private String websocketSessionId;
    private ICurrency currency;
    private boolean showRefreshButton;
    private List<Long> stakes;
    //number of stakes to be transferred from balance
    private int stakesReserve;
    //limit of stakes after which you need to make additional buyIn
    private int stakesLimit;
    private double lbContributionPercent;
    private long gameId;
    private MaxQuestWeaponMode weaponMode;
    private boolean allowWeaponSaveInAllGames;
    private transient ISocketClient socketClient;
    private MoneyType moneyType;
    private IActiveFrbSession activeFrbSession;
    private ActiveCashBonusSession activeCashBonusSession;
    private TournamentSession tournamentSession;
    private boolean confirmBattlegroundBuyIn;
    private boolean battlegroundAllowed;
    private List<Long> battlegroundBuyIns;
    private double battlegroundRakePercent;
    private boolean privateRoom;
    private boolean roomManager;
    private boolean sendRealBetWin;
    private String externalId;

    public LobbySession() {}

    public LobbySession(String sessionId, long accountId, long bankId, String nickname, Avatar avatar, long enterDate,
                        long balance, long roomId, ISocketClient socketClient, Currency currency,
                        boolean showRefreshButton, List<Long> stakes, int stakesReserve, int stakesLimit,
                        double lbContributionPercent, long gameId, MaxQuestWeaponMode weaponMode,
                        boolean allowWeaponSaveInAllGames, MoneyType moneyType, IActiveFrbSession activeFrbSession,
                        ActiveCashBonusSession activeCashBonusSession, TournamentSession tournamentSession) {
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.bankId = bankId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.enterDate = enterDate;
        this.balance = balance;
        this.roomId = roomId;
        this.socketClient = socketClient;
        this.currency = currency;
        this.showRefreshButton = showRefreshButton;
        this.stakes = stakes;
        this.stakesReserve = stakesReserve;
        this.stakesLimit = stakesLimit;
        this.lbContributionPercent = lbContributionPercent;
        this.gameId = gameId;
        this.weaponMode = weaponMode;
        this.allowWeaponSaveInAllGames = allowWeaponSaveInAllGames;
        this.moneyType = moneyType;
        this.activeFrbSession = activeFrbSession;
        this.activeCashBonusSession = activeCashBonusSession;
        this.tournamentSession = tournamentSession;
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
    public long getAccountId() {
        return accountId;
    }

    @Override
    public void setAccountId(long accountId) {
        this.accountId = accountId;
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
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public boolean isNicknameEditable() {
        return nicknameEditable;
    }

    @Override
    public void setNicknameEditable(boolean nicknameEditable) {
        this.nicknameEditable = nicknameEditable;
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
    public long getEnterDate() {
        return enterDate;
    }

    @Override
    public void setEnterDate(long enterDate) {
        this.enterDate = enterDate;
    }

    @Override
    public long getBalance() {
        if (activeCashBonusSession != null) {
            return activeCashBonusSession.getBalance();
        } else if (tournamentSession != null) {
            return tournamentSession.getBalance();
        }
        return balance;
    }

    @Override
    public void setBalance(long balance) {
        if (activeCashBonusSession != null) {
            activeCashBonusSession.setBalance(balance);
        } else if (tournamentSession != null) {
            tournamentSession.setBalance(balance);
        }
        this.balance = balance;
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
    public ISocketClient getSocketClient() {
        return socketClient;
    }

    @Override
    public void setSocketClient(ISocketClient socketClient) {
        this.socketClient = socketClient;
    }

    @Override
    public boolean isDisableTooltips() {
        return disableTooltips;
    }

    @Override
    public void setDisableTooltips(boolean disableTooltips) {
        this.disableTooltips = disableTooltips;
    }

    @Override
    public String getWebsocketSessionId() {
        return websocketSessionId;
    }

    @Override
    public void setWebsocketSessionId(String websocketSessionId) {
        this.websocketSessionId = websocketSessionId;
    }

    @Override
    public ICurrency getCurrency() {
        return currency;
    }

    @Override
    public void setCurrency(ICurrency currency) {
        this.currency = currency;
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
    public List<Long> getStakes() {
        return stakes;
    }

    @Override
    public void setStakes(List<Long> stakes) {
        this.stakes = stakes;
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
    public int getStakesLimit() {
        return stakesLimit;
    }

    @Override
    public void setStakesLimit(int stakesLimit) {
        this.stakesLimit = stakesLimit;
    }

    @Override
    public double getLbContributionPercent() {
        return lbContributionPercent;
    }

    @Override
    public void setLbContributionPercent(double lbContributionPercent) {
        this.lbContributionPercent = lbContributionPercent;
    }

    @Override
    public boolean isLeaderboardDisabled() {
        return lbContributionPercent <= 0;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(long gameId) {
        this.gameId = gameId;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LobbySession that = (LobbySession) o;
        return accountId == that.accountId &&
                Objects.equals(sessionId, that.sessionId);
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
    public MoneyType getMoneyType() {
        return moneyType;
    }

    @Override
    public void setMoneyType(MoneyType moneyType) {
        this.moneyType = moneyType;
    }

    @Override
    public IActiveFrbSession getActiveFrbSession() {
        return activeFrbSession;
    }

    @Override
    public void setActiveFrbSession(IActiveFrbSession activeFrbSession) {
        this.activeFrbSession = activeFrbSession == null ? null : (ActiveFrbSession) activeFrbSession;
    }

    @Override
    public ActiveCashBonusSession getActiveCashBonusSession() {
        return activeCashBonusSession;
    }

    @Override
    public void setActiveCashBonusSession(IActiveCashBonusSession activeCashBonusSession) {
        this.activeCashBonusSession = activeCashBonusSession == null ? null : (ActiveCashBonusSession) activeCashBonusSession;
    }

    @Override
    public TournamentSession getTournamentSession() {
        return tournamentSession;
    }

    @Override
    public void setTournamentSession(ITournamentSession tournamentSession) {
        this.tournamentSession = (TournamentSession) tournamentSession;
    }

    @Override
    public boolean isConfirmBattlegroundBuyIn() {
        return confirmBattlegroundBuyIn;
    }

    @Override
    public void setConfirmBattlegroundBuyIn(boolean confirmBattlegroundBuyIn) {
        this.confirmBattlegroundBuyIn = confirmBattlegroundBuyIn;
    }

    @Override
    public boolean isBattlegroundAllowed() {
        return battlegroundAllowed;
    }

    @Override
    public void setBattlegroundAllowed(boolean battlegroundAllowed) {
        this.battlegroundAllowed = battlegroundAllowed;
    }

    @Override
    public List<Long> getBattlegroundBuyIns() {
        return battlegroundBuyIns;
    }

    @Override
    public void setBattlegroundBuyIns(List<Long> battlegroundBuyIns) {
        this.battlegroundBuyIns = battlegroundBuyIns;
    }

    @Override
    public double getBattlegroundRakePercent() {
        return battlegroundRakePercent;
    }

    @Override
    public void setBattlegroundRakePercent(double battlegroundRakePercent) {
        this.battlegroundRakePercent = battlegroundRakePercent;
    }

    public boolean isSendRealBetWin() {
        return sendRealBetWin;
    }

    public void setSendRealBetWin(boolean sendRealBetWin) {
        this.sendRealBetWin = sendRealBetWin;
    }

    @Override
    public boolean isPrivateRoom() {
        return privateRoom;
    }

    @Override
    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }

    @Override
    public boolean isOwner() {
        return roomManager;
    }

    @Override
    public void setOwner(boolean owner) {
        this.roomManager = owner;
    }

    @Override
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public String getExternalId() {
        return externalId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LobbySession [");
        sb.append("sessionId=").append(sessionId);
        sb.append(", accountId=").append(accountId);
        sb.append(", bankId=").append(bankId);
        sb.append(", gameId=").append(gameId);
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", nicknameEditable=").append(nicknameEditable);
        sb.append(", avatar=").append(avatar);
        sb.append(", enterDate=").append(enterDate);
        sb.append(", balance=").append(balance);
        sb.append(", roomId=").append(roomId);
        sb.append(", disableTooltips=").append(disableTooltips);
        sb.append(", websocketSessionId=").append(websocketSessionId);
        sb.append(", currency=").append(currency);
        sb.append(", showRefreshButton=").append(showRefreshButton);
        sb.append(", stakes=").append(stakes);
        sb.append(", stakesReserve=").append(stakesReserve);
        sb.append(", stakesLimit=").append(stakesLimit);
        sb.append(", lbContributionPercent=").append(lbContributionPercent);
        sb.append(", weaponMode=").append(weaponMode);
        sb.append(", allowWeaponSaveInAllGames=").append(allowWeaponSaveInAllGames);
        sb.append(", moneyType=").append(moneyType);
        sb.append(", activeFrbSession=").append(activeFrbSession);
        sb.append(", activeCashBonusSession=").append(activeCashBonusSession);
        sb.append(", tournamentSession=").append(tournamentSession);
        sb.append(", isConfirmBattlegroundBuyIn=").append(confirmBattlegroundBuyIn);
        sb.append(", battlegroundAllowed=").append(battlegroundAllowed);
        sb.append(", battlegroundBuyIns=").append(battlegroundBuyIns);
        sb.append(", battlegroundRakePercent=").append(battlegroundRakePercent);
        sb.append(", privateRoom=").append(privateRoom);
        sb.append(", isOwner=").append(roomManager);
        sb.append(", sendRealBetWin=").append(sendRealBetWin);

        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(sessionId);
        output.writeLong(accountId, true);
        output.writeLong(bankId, true);
        output.writeString(nickname);
        kryo.writeClassAndObject(output, avatar);
        output.writeLong(enterDate, true);
        output.writeLong(balance, true);
        output.writeLong(roomId, true);
        output.writeBoolean(disableTooltips);
        output.writeString(websocketSessionId);
        kryo.writeClassAndObject(output, currency);
        output.writeBoolean(showRefreshButton);
        kryo.writeClassAndObject(output, stakes);
        output.writeInt(stakesReserve, true);
        output.writeInt(stakesLimit, true);
        output.writeDouble(lbContributionPercent);
        output.writeLong(gameId, true);
        output.writeInt(getWeaponMode().ordinal(), true);
        output.writeBoolean(allowWeaponSaveInAllGames);
        output.writeString(moneyType.name());
        kryo.writeClassAndObject(output, activeFrbSession);
        kryo.writeClassAndObject(output, activeCashBonusSession);
        kryo.writeClassAndObject(output, tournamentSession);
        output.writeBoolean(nicknameEditable);
        output.writeBoolean(confirmBattlegroundBuyIn);
        output.writeBoolean(battlegroundAllowed);
        kryo.writeClassAndObject(output, battlegroundBuyIns);
        output.writeDouble(battlegroundRakePercent);
        output.writeBoolean(privateRoom);
        output.writeBoolean(roomManager);
        output.writeBoolean(sendRealBetWin);
        output.writeString(externalId);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        sessionId = input.readString();
        accountId = input.readLong(true);
        bankId = input.readLong(true);
        nickname = input.readString();
        avatar = (IAvatar) kryo.readClassAndObject(input);
        enterDate = input.readLong(true);
        balance = input.readLong(true);
        roomId = input.readLong(true);
        disableTooltips = input.readBoolean();
        websocketSessionId = input.readString();
        currency = (ICurrency) kryo.readClassAndObject(input);
        showRefreshButton = input.readBoolean();
        //noinspection unchecked
        stakes = (List<Long>) kryo.readClassAndObject(input);
        stakesReserve = input.readInt(true);
        stakesLimit = input.readInt(true);
        lbContributionPercent = input.readDouble();
        gameId = input.readLong(true);
        weaponMode = MaxQuestWeaponMode.valueOf(input.readInt(true));
        allowWeaponSaveInAllGames = input.readBoolean();
        moneyType = MoneyType.valueOf(input.readString());
        activeFrbSession = (IActiveFrbSession) kryo.readClassAndObject(input);
        activeCashBonusSession = (ActiveCashBonusSession) kryo.readClassAndObject(input);
        tournamentSession = (TournamentSession) kryo.readClassAndObject(input);
        nicknameEditable = input.readBoolean();
        confirmBattlegroundBuyIn = input.readBoolean();
        battlegroundAllowed = input.readBoolean();
        //noinspection unchecked
        battlegroundBuyIns = (List<Long>) kryo.readClassAndObject(input);
        battlegroundRakePercent = input.readDouble();
        if (version > 0) {
            privateRoom = input.readBoolean();
            roomManager = input.readBoolean();
        }
        if (version > 1) {
            sendRealBetWin = input.readBoolean();
        }
        if (version > 2) {
            externalId = input.readString();
        }
    }
}
