package com.betsoft.casino.bots;

import com.betsoft.casino.mp.common.math.SWPaidCosts;

import java.util.List;

public interface ILobbyBot extends IBot {
    default void setBalance(long balance) {
    }
    default void sendGetStartGameUrlRequest() {
    }
    void pickNickname(boolean retry, String enterLobbyNickname);
    void pickAvatar();
    void setStakes(List<Float> stakes);
    void setStakesLimit(int limit);
    void setWeaponPrices(List<SWPaidCosts> weaponPrices);
    Integer getWeaponPriceById(int id);

    default void setMinStake(Long minStake) {
    }

    default void setMaxStake(Long maxStake) {
    }
    default void setBuyIns(List<Long> stakes) {
    }

    default boolean isSpecificRoom(){return false;}

    default void setSpecificRoom(boolean isSpecificRoom) {}

    default void setRoomFromArgs(long roomId){};
}
