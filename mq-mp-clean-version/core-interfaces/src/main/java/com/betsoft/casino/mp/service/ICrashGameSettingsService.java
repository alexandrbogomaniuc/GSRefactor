package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ICrashGameSetting;

import java.util.Collection;

/**
 * User: flsh
 * Date: 22.04.2022.
 */
public interface ICrashGameSettingsService {
    Collection<ICrashGameSetting> getSettings();
    ICrashGameSetting getSettings(long bankId, long gameId);
    ICrashGameSetting getSettings(long bankId, long gameId, String roomCurrency);
}
