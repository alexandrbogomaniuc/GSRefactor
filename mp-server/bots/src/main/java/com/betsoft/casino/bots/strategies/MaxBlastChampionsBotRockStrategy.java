package com.betsoft.casino.bots.strategies;

import javax.script.ScriptEngine;

import com.betsoft.casino.mp.maxblastchampions.model.PlayGameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

public class MaxBlastChampionsBotRockStrategy extends MaxBlastChampionsBotStrategy {
    private static final Logger LOG = LogManager.getLogger(MaxBlastChampionsBotRockStrategy.class);

    public MaxBlastChampionsBotRockStrategy(ScriptEngine scriptEngine, long requestedByInAmount, long minAllowedTime, long percentMaxAllowedTimeMs) {
        super(scriptEngine, minAllowedTime, (PlayGameState.MAX_CRASH_MILLISECONDS * percentMaxAllowedTimeMs / 100), requestedByInAmount);
        LOG.info("MaxBlastChampionsBotRockStrategy::MaxBlastChampionsBotRockStrategy: " +
                        "requestedByInAmount={}, minAllowedTime={}, percentMaxAllowedTimeMs={}",
                requestedByInAmount, minAllowedTime, percentMaxAllowedTimeMs);
    }

    @Override
    public long getCrashBetRequestTime(long currentTime, long roundStartTime) {

        LOG.info("MaxBlastChampionsBotRockStrategy::getCrashBetRequestTime: bot=({},{}), currentTime={} roundStartTime={}",
                bot != null ? bot.getId() : null, bot != null ? bot.getNickname() : null,
                toHumanReadableFormat(currentTime), toHumanReadableFormat(roundStartTime));
        //Medium: Random from second 4 to 10, total 15 sec
        long minTime = roundStartTime - 11 * 1000;
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
