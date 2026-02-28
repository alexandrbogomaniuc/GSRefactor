package com.abs.casino.common.web.statistics;

import com.dgphoenix.casino.common.web.statistics.StatisticsManager;

public class StatisticsBuilder {
    private static StatisticsBuilder instance = new StatisticsBuilder();

    private StatisticsBuilder() {
    }

    public static StatisticsBuilder getInstance() {
        return instance;
    }

    public String buildRequestStatistics() {
        if (!StatisticsManager.getInstance().isEnableStatistics()) {
            return "Statistics is not enabled";
        }

        StringBuilder sb = new StringBuilder();
        StatisticsManager.getInstance().printRequestStatistics(sb, true);
        return sb.toString();
    }
}
