package com.betsoft.casino.mp.model.gameconfig;

import com.betsoft.casino.mp.model.Money;

import java.util.Collections;
import java.util.Map;

public interface IGameConfig {
    Map<Integer, Integer> getWeaponPrices();

    default Map<Integer, Double> getGemPrizes(Money stake, int betLevel) {
        return Collections.emptyMap();
    }
}
