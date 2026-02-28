package com.betsoft.casino.mp.model;

import com.abs.casino.common.cache.data.currency.ICurrency;
import com.betsoft.casino.mp.common.math.Paytable;
import com.betsoft.casino.mp.model.battleground.ITransportObserver;
import com.betsoft.casino.mp.model.onlineplayer.Friend;
import com.betsoft.casino.utils.ITransportObject;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IEnterLobbyResponse<CURRENCY extends ICurrency,
        AVATAR extends IAvatar,
        FR_BONUS_INFO extends IFRBonusInfo,
        CASH_BONUS_INFO extends ICashBonusInfo,
        TOURNAMENT_INFO extends ITournamentInfo,
        ENTER_LOBBY_BG_INFO extends IEnterLobbyBattlegroundInfo> {

    int getPlayers();

    void setPlayers(int players);

    String getNickname();

    void setNickname(String nickname);

    boolean isNicknameEditable();

    void setNicknameEditable(boolean nicknameEditable);
    long getBalance();

    void setBalance(long balance);

    CURRENCY getCurrency();

    void setCurrency(CURRENCY currency);

    long getRankPoints();

    void setRankPoints(int rankPoints);

    Long getRoomId();

    void setRoomId(Long roomId);

    int getLevel();

    AVATAR getAvatar();

    void setAvatar(AVATAR avatar);

    List<Integer> getBorders();

    void setBorders(List<Integer> borders);

    List<Integer> getHeroes();

    void setHeroes(List<Integer> heroes);

    List<Integer> getBackgrounds();

    void setBackgrounds(List<Integer> backgrounds);

    FR_BONUS_INFO getFrBonusInfo();

    void setFrBonusInfo(FR_BONUS_INFO frBonusInfo);

    CASH_BONUS_INFO getCashBonusInfo();

    void setCashBonusInfo(CASH_BONUS_INFO cashBonusInfo);

    TOURNAMENT_INFO getTournamentInfo();

    void setTournamentInfo(TOURNAMENT_INFO tournamentInfo);

    boolean isShowRefreshBalanceButton();

    long getKills();

    long getTreasures();

    int getRounds();

    long getXp();

    long getXpPrev();

    long getXpNext();

    boolean isDisableTooltips();

    List<Float> getStakes();

    void setStakes(List<Float> stakes);

    int getStakesReserve();

    void setStakesReserve(int stakesReserve);

    int getStakesLimit();

    void setStakesLimit(int stakesLimit);

    float getAlreadySitInStake();

    void setAlreadySitInStake(float alreadySitInStake);

    boolean isNeedStartBonus();

    void setNeedStartBonus(boolean needStartBonus);

    String getNicknameGlyphs();

    void setNicknameGlyphs(String nicknameGlyphs);

    String getWeaponMode();

    void setWeaponMode(String weaponMode);

    int getMaxBulletsLimitOnMap();

    void setMaxBulletsLimitOnMap(int maxBulletsLimitOnMap);

    ENTER_LOBBY_BG_INFO getBattleground();

    void setBattleground(ENTER_LOBBY_BG_INFO battleground);

    Long getMinStake();

    void setMinStake(Long minStake);

    Long getMaxStake();

    void setMaxStake(Long maxStake);

    Paytable getPaytable();
}
