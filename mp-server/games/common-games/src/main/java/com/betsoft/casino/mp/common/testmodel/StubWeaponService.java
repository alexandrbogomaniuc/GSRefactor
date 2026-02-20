package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.service.IWeaponService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: flsh
 * Date: 20.02.2020.
 */
public class StubWeaponService implements IWeaponService {
    private Map<String, Map<Money, Map<Integer, Integer>>> storage = new ConcurrentHashMap<>();

    @Override
    public void saveWeapons(long bankId, long accountId, int mode, Money stake, Map<Integer, Integer> weapons,
                            long gameId) {
        String key = composeKey(bankId, accountId, mode);
        if (!storage.containsKey(key)) {
            storage.put(key, new ConcurrentHashMap<>());
        }
        storage.get(key).put(stake, weapons);
    }

    @Override
    public void saveSpecialModeWeapons(long tournamentOrBonusId, long accountId, int mode, Money stake,
                                       Map<Integer, Integer> weapons, long gameId) {

    }

    @Override
    public Map<Integer, Integer> loadWeapons(long bankId, long accountId, int mode, Money stake, long gameId) {
        return storage
                .getOrDefault(composeKey(bankId, accountId, mode), new HashMap<>())
                .getOrDefault(stake, new HashMap<>());
    }

    @Override
    public Map<Integer, Integer> loadSpecialModeWeapons(long tournamentOrBonusId, long accountId, int mode,
                                                        Money stake, long gameId) {
        return null;
    }

    @Override
    public Map<Money, Map<Integer, Integer>> getAllWeapons(long bankId, long accountId, int mode, long gameId) {
        return storage.getOrDefault(composeKey(bankId, accountId, mode), new HashMap<>());
    }

    @Override
    public Map<Money, Map<Integer, Integer>> getAllSpecialModeWeapons(long tournamentOrBonusId, long accountId,
                                                                      int mode, long gameId) {
        return null;
    }

    @Override
    public Map<Long, Map<Integer, Integer>> getAllWeaponsLong(long bankId, long accountId, int mode, long gameId) {
        return null;
    }

    @Override
    public Map<Long, Map<Integer, Integer>> getSpecialModeAllWeaponsLong(long tournamentOrBonusId, long accountId,
                                                                         int mode, long gameId) {
        return null;
    }

    @Override
    public void removeAllWeapons() {

    }

    private String composeKey(long bankId, long accountId, int mode) {
        return bankId + "+" + accountId + "+" + mode;
    }
}
