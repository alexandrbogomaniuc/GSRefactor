package com.dgphoenix.casino.common.web.statistics;

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
