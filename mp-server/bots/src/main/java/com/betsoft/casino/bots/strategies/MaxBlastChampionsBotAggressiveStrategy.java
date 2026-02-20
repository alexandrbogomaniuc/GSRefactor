package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.mp.maxblastchampions.model.PlayGameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptEngine;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

public class MaxBlastChampionsBotAggressiveStrategy extends MaxBlastChampionsBotStrategy {
    private static final Logger LOG = LogManager.getLogger(MaxBlastChampionsBotAggressiveStrategy.class);

    public MaxBlastChampionsBotAggressiveStrategy(ScriptEngine scriptEngine, long requestedByInAmount, long minAllowedTime, long percentMaxAllowedTimeMs) {
        super(scriptEngine, minAllowedTime, (PlayGameState.MAX_CRASH_MILLISECONDS * percentMaxAllowedTimeMs / 100), requestedByInAmount);
        LOG.info("MaxBlastChampionsBotAggressiveStrategy::MaxBlastChampionsBotAggressiveStrategy: " +
                        "requestedByInAmount={}, minAllowedTime={}, percentMaxAllowedTimeMs={}",
                requestedByInAmount, minAllowedTime, percentMaxAllowedTimeMs);
    }

    @Override
    public long getCrashBetRequestTime(long currentTime, long roundStartTime) {
        long crashBetRequestTime;

        LOG.info("MaxBlastChampionsBotAggressiveStrategy::getCrashBetRequestTime: bot=({},{}), currentTime={} roundStartTime={}",
                bot != null ? bot.getId() : null, bot != null ? bot.getNickname() : null,
                toHumanReadableFormat(currentTime), toHumanReadableFormat(roundStartTime));
        //Medium: Random from second 6 to 10, total 15 sec
        long minTime = roundStartTime - 9 * 1000;
        long maxTime = roundStartTime - 5 * 1000;

        if(minTime < currentTime) {
            minTime = currentTime;
        }

        if(maxTime < currentTime) {
            maxTime = currentTime;
        }

        return super.getCrashBetRequestTime(minTime, maxTime);
    }
}
