package com.betsoft.casino.bots.strategies;

import com.betsoft.casino.bots.model.EnemySize;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

public interface IRoomNaturalBotStrategy extends IRoomBotStrategy{
    void addLastShootResponseTime(String key, Long time);

    Long getTimesBulletLastResponseByType(String key);

    Long getTimesShootLastResponseByType(String key);

    Long getTimesMinResponseByType(String key);

    void updateShootResponseTimeMetric(String metric, Long value);

    void updateShootRequestTimeMetric(String metric, Long value);

    void updateBulletResponseTimeMetric(String metric, Long value);

    void updateBulletRequestTimeMetric(String metric, Long value);

    void updateOtherResponseTimeMetric(String metric, Long value);

    void updateOtherRequestTimeMetric(String metric, Long value);

    EnemySize getEnemySize(long typeId);

    boolean isBulletTime(String botId);

    boolean isRicochetWeapon();

    long getWaitTimeForSwitchWeapon(String botId);

    default long getWaitTime(String botId, String key,
                             HashMap<String, Long> timesLastRequestByType,
                             HashMap<String, Long> timesLastResponseByType,
                             HashMap<String, Long> timesMinResponseByType,
                             boolean debug, Logger LOG) {

        Long lastTimeRequest = timesLastRequestByType.get(key);

        Map.Entry<String, Long> maxTimesLastRequestByType = timesLastRequestByType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if(maxTimesLastRequestByType != null) {

            Long maxTimeRequestAll = maxTimesLastRequestByType.getValue();
            if (maxTimeRequestAll > lastTimeRequest) {

                LOG.debug("getWaitTime: bot:{} lastTimeRequest:{} by the key:{}, max time all weapons:{}",
                        botId, key, toHumanReadableFormat(lastTimeRequest), toHumanReadableFormat(maxTimeRequestAll));

                lastTimeRequest = maxTimeRequestAll;
            }
        }

        /*Long lastTimeResponse = timesLastResponseByType.get(key);

        Map.Entry<String, Long> maxTimesLastResponseByType = timesLastResponseByType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if(maxTimesLastResponseByType != null) {

            Long maxTimeResponseAll = maxTimesLastResponseByType.getValue();
            if (maxTimeResponseAll > lastTimeResponse) {

                LOG.debug("getWaitTime: bot:{} lastTimeResponse:{} by the key:{}, max time all weapons:{}",
                        botId, key, toHumanReadableFormat(lastTimeResponse), toHumanReadableFormat(maxTimeResponseAll));

                lastTimeResponse = maxTimeResponseAll;
            }
        }*/

        Long minWait = timesMinResponseByType.get(key);
        long currentTimeMillis = System.currentTimeMillis();
        long waitRandomTime = RNG.nextInt(0, 40);
        long lastTime = lastTimeRequest;//Math.max(lastTimeRequest, lastTimeResponse);
        long randomNextTime = lastTime + minWait + waitRandomTime;

        if (lastTime != 0 && currentTimeMillis >= randomNextTime) {

            if (debug) {
                LOG.debug("getWaitTime: bot:{}, lastTime:{}, key:{}, " +
                                "minWait:{}, waitRandomTime:{}, currentTimeMillis:{}, randomNextTime:{}",
                        botId, toHumanReadableFormat(lastTime), key, minWait, waitRandomTime,
                        toHumanReadableFormat(currentTimeMillis), toHumanReadableFormat(randomNextTime));
            }

            return 0;
        }

        long waitForSleep = lastTime == 0 ? minWait : (randomNextTime - currentTimeMillis);
        waitForSleep = Math.min(waitForSleep, minWait);

        if (debug) {
            LOG.debug("getWaitTime: bot:{}, lastTime:{}, key:{}, minWait:{}, " +
                            "waitRandomTime:{}, waitForSleep:{}",
                    botId, toHumanReadableFormat(lastTime), key, minWait,
                    toHumanReadableFormat(waitRandomTime), waitForSleep);
        }

        LOG.debug("getWaitTime: bot:{} finish, need shot", botId);
        return waitForSleep;
    }
}
