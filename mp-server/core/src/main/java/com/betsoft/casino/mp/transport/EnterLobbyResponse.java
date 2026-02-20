package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.common.math.Paytable;
import com.betsoft.casino.mp.model.IEnterLobbyResponse;
import com.betsoft.casino.utils.TObject;

import java.util.List;
import java.util.StringJoiner;

/**
 * User: flsh
 * Date: 06.06.17.
 */

public class EnterLobbyResponse extends TObject implements IEnterLobbyResponse<Currency, Avatar, FRBonusInfo, CashBonusInfo, TournamentInfo, EnterLobbyBattlegroundInfo> {
    private int players;
    private String nickname;
    private boolean nicknameEditable = true;
    private long balance;
    private Currency currency;
    private long rankPoints;
    private long roomId;
    private int level;
    private Avatar avatar;
    //all available (free and purchased) borders, heroes, backgrounds
    private List<Integer> borders;
    private List<Integer> heroes;
    private List<Integer> backgrounds;
    private List<Float> stakes;
    private Paytable paytable;
    private FRBonusInfo frBonusInfo;
    private CashBonusInfo cashBonusInfo;
    private TournamentInfo tournamentInfo;
    private boolean showRefreshBalanceButton;
    private long kills;
    private long treasures;
    private int rounds;
    private long xp;
    private long xpPrev;
    private long xpNext;
    private boolean disableTooltips;
    private boolean needStartBonus;
    private String nicknameGlyphs;

    private int stakesReserve;
    private int stakesLimit;
    private float alreadySitInStake;

    private String weaponMode;
    private int maxBulletsLimitOnMap;
    private EnterLobbyBattlegroundInfo battleground;

    private Long minStake;
    private Long maxStake;

    public EnterLobbyResponse(long date, int players, String nickname, long balance, int rid, Currency currency,
                              long rankPoints, long roomId, int level, Avatar avatar, List<Float> stakes,
                              List<Integer> borders, List<Integer> heroes, List<Integer> backgrounds,
                              Paytable paytable, FRBonusInfo frBonusInfo,
                              boolean showRefreshBalanceButton, long kills, long treasures, int rounds, long xp,
                              long xpPrev, long xpNext, boolean disableTooltips, int stakesReserve,
                              int stakesLimit, float alreadySitInStake,
                              boolean needStartBonus, String nicknameGlyphs, String weaponMode,
                              int maxBulletsLimitOnMap, EnterLobbyBattlegroundInfo battleground) {
        super(date, rid);
        this.players = players;
        this.nickname = nickname;
        this.balance = balance;
        this.currency = currency;
        this.rankPoints = rankPoints;
        this.roomId = roomId;
        this.level = level;
        this.avatar = avatar;
        this.stakes = stakes;
        this.borders = borders;
        this.heroes = heroes;
        this.backgrounds = backgrounds;
        this.paytable = paytable;
        this.frBonusInfo = frBonusInfo;
        this.showRefreshBalanceButton = showRefreshBalanceButton;
        this.kills = kills;
        this.treasures = treasures;
        this.rounds = rounds;
        this.xp = xp;
        this.xpPrev = xpPrev;
        this.xpNext = xpNext;
        this.disableTooltips = disableTooltips;
        this.stakesReserve = stakesReserve;
        this.stakesLimit = stakesLimit;
        this.alreadySitInStake = alreadySitInStake;
        this.needStartBonus = needStartBonus;
        this.nicknameGlyphs = nicknameGlyphs;
        this.weaponMode = weaponMode;
        this.maxBulletsLimitOnMap = maxBulletsLimitOnMap;
        this.battleground = battleground;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isNicknameEditable() {
        return nicknameEditable;
    }

    public void setNicknameEditable(boolean nicknameEditable) {
        this.nicknameEditable = nicknameEditable;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public long getRankPoints() {
        return rankPoints;
    }

    public void setRankPoints(int rankPoints) {
        this.rankPoints = rankPoints;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public int getLevel() {
        return level;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public List<Integer> getBorders() {
        return borders;
    }

    public void setBorders(List<Integer> borders) {
        this.borders = borders;
    }

    public List<Integer> getHeroes() {
        return heroes;
    }

    public void setHeroes(List<Integer> heroes) {
        this.heroes = heroes;
    }

    public List<Integer> getBackgrounds() {
        return backgrounds;
    }

    public void setBackgrounds(List<Integer> backgrounds) {
        this.backgrounds = backgrounds;
    }

    public FRBonusInfo getFrBonusInfo() {
        return frBonusInfo;
    }

    public void setFrBonusInfo(FRBonusInfo frBonusInfo) {
        this.frBonusInfo = frBonusInfo;
    }

    public CashBonusInfo getCashBonusInfo() {
        return cashBonusInfo;
    }

    public void setCashBonusInfo(CashBonusInfo cashBonusInfo) {
        this.cashBonusInfo = cashBonusInfo;
    }

    public TournamentInfo getTournamentInfo() {
        return tournamentInfo;
    }

    public void setTournamentInfo(TournamentInfo tournamentInfo) {
        this.tournamentInfo = tournamentInfo;
    }

    public boolean isShowRefreshBalanceButton() {
        return showRefreshBalanceButton;
    }

    public long getKills() {
        return kills;
    }

    public long getTreasures() {
        return treasures;
    }

    public int getRounds() {
        return rounds;
    }

    public long getXp() {
        return xp;
    }

    public long getXpPrev() {
        return xpPrev;
    }

    public long getXpNext() {
        return xpNext;
    }

    public boolean isDisableTooltips() {
        return disableTooltips;
    }

    public List<Float> getStakes() {
        return stakes;
    }

    public void setStakes(List<Float> stakes) {
        this.stakes = stakes;
    }

    public int getStakesReserve() {
        return stakesReserve;
    }

    public void setStakesReserve(int stakesReserve) {
        this.stakesReserve = stakesReserve;
    }

    public int getStakesLimit() {
        return stakesLimit;
    }

    public void setStakesLimit(int stakesLimit) {
        this.stakesLimit = stakesLimit;
    }

    public float getAlreadySitInStake() {
        return alreadySitInStake;
    }

    public void setAlreadySitInStake(float alreadySitInStake) {
        this.alreadySitInStake = alreadySitInStake;
    }

    public boolean isNeedStartBonus() {
        return needStartBonus;
    }

    public void setNeedStartBonus(boolean needStartBonus) {
        this.needStartBonus = needStartBonus;
    }

    public String getNicknameGlyphs() {
        return nicknameGlyphs;
    }

    public void setNicknameGlyphs(String nicknameGlyphs) {
        this.nicknameGlyphs = nicknameGlyphs;
    }

    public String getWeaponMode() {
        return weaponMode;
    }

    public void setWeaponMode(String weaponMode) {
        this.weaponMode = weaponMode;
    }

    public int getMaxBulletsLimitOnMap() {
        return maxBulletsLimitOnMap;
    }

    public void setMaxBulletsLimitOnMap(int maxBulletsLimitOnMap) {
        this.maxBulletsLimitOnMap = maxBulletsLimitOnMap;
    }

    public EnterLobbyBattlegroundInfo getBattleground() {
        return battleground;
    }

    public void setBattleground(EnterLobbyBattlegroundInfo battleground) {
        this.battleground = battleground;
    }

    public Long getMinStake() {
        return minStake;
    }

    public void setMinStake(Long minStake) {
        this.minStake = minStake;
    }

    public Long getMaxStake() {
        return maxStake;
    }

    public void setMaxStake(Long maxStake) {
        this.maxStake = maxStake;
    }

    public Paytable getPaytable() {
        return paytable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnterLobbyResponse that = (EnterLobbyResponse) o;

        if (players != that.players) return false;
        if (balance != that.balance) return false;
        return nickname.equals(that.nickname);
    }

    @Override
    public int hashCode() {
        return nickname.hashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EnterLobbyResponse.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("players=" + players)
                .add("nickname='" + nickname + "'")
                .add("nicknameEditable=" + nicknameEditable)
                .add("balance=" + balance)
                .add("currency=" + currency)
                .add("rankPoints=" + rankPoints)
                .add("roomId=" + roomId)
                .add("level=" + level)
                .add("avatar=" + avatar)
                .add("borders=" + borders)
                .add("heroes=" + heroes)
                .add("backgrounds=" + backgrounds)
                .add("stakes=" + stakes)
                .add("paytable=" + paytable)
                .add("frBonusInfo=" + frBonusInfo)
                .add("cashBonusInfo=" + cashBonusInfo)
                .add("tournamentInfo=" + tournamentInfo)
                .add("showRefreshBalanceButton=" + showRefreshBalanceButton)
                .add("kills=" + kills)
                .add("treasures=" + treasures)
                .add("rounds=" + rounds)
                .add("xp=" + xp)
                .add("xpPrev=" + xpPrev)
                .add("xpNext=" + xpNext)
                .add("disableTooltips=" + disableTooltips)
                .add("needStartBonus=" + needStartBonus)
                .add("nicknameGlyphs='" + nicknameGlyphs + "'")
                .add("stakesReserve=" + stakesReserve)
                .add("stakesLimit=" + stakesLimit)
                .add("alreadySitInStake=" + alreadySitInStake)
                .add("weaponMode='" + weaponMode + "'")
                .add("maxBulletsLimitOnMap=" + maxBulletsLimitOnMap)
                .add("battleground=" + battleground)
                .add("minStake=" + minStake)
                .add("maxStake=" + maxStake)
                .toString();
    }
}
