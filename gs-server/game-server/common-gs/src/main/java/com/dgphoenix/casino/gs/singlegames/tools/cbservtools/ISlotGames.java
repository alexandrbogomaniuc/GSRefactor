package com.abs.casino.gs.singlegames.tools.cbservtools;

import com.google.common.base.Splitter;

import java.util.Collection;
import java.util.Map;

public interface ISlotGames extends ICoinGame {

    Splitter.MapSplitter COIN_PROGRESS_SPLITTER = Splitter.on("|").withKeyValueSeparator(Splitter.on("-").limit(2));

    String PARAM_COIN_PROGRESS = "COIN_PROGRESS";

    String getDefaultBetPerLineByServlet();

    String getDefaultNumLineByServlet();

    Collection<Double> getProgressiveCoins(Map<String, String> lasthand);
}
