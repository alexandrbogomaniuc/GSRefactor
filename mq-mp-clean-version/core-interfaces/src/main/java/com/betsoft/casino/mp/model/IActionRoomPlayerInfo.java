package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.battleground.IBgPlace;

import java.util.Map;

public interface IActionRoomPlayerInfo extends IRoomPlayerInfo {

    int getSpecialWeaponId();

    void setSpecialWeaponId(int specialWeaponId);

    boolean isAllowWeaponSaveInAllGames();

    void setAllowWeaponSaveInAllGames(boolean allowWeaponSaveInAllGames);

    Map<Integer, Integer> getWeapons();

    void setWeapons(Map<Integer, Integer> weapons);

    MaxQuestWeaponMode getWeaponMode();

    void setWeaponMode(MaxQuestWeaponMode weaponMode);

    IFreeShots createNewFreeShots();

    IMinePlace getNewMinePlace(long date, int rid, int seatId, float x, float y, String mineId);

}
