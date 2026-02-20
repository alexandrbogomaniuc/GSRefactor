package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.Money;

import java.util.Map;

public interface IWeaponService {
    void saveWeapons(long bankId, long accountId, int mode, Money stake, Map<Integer, Integer> weapons, long gameId);

    void saveSpecialModeWeapons(long tournamentOrBonusId, long accountId, int mode, Money stake, Map<Integer, Integer> weapons, long gameId);

    Map<Integer, Integer> loadWeapons(long bankId, long accountId, int mode, Money stake, long gameId);

    Map<Integer, Integer> loadSpecialModeWeapons(long tournamentOrBonusId, long accountId, int mode, Money stake, long gameId);

    Map<Money, Map<Integer, Integer>> getAllWeapons(long bankId, long accountId, int mode, long gameId);

    Map<Money, Map<Integer, Integer>> getAllSpecialModeWeapons(long tournamentOrBonusId, long accountId, int mode, long gameId);

    Map<Long, Map<Integer, Integer>> getAllWeaponsLong(long bankId, long accountId, int mode, long gameId);

    Map<Long, Map<Integer, Integer>> getSpecialModeAllWeaponsLong(long tournamentOrBonusId, long accountId, int mode, long gameId);

    void removeAllWeapons();
}
