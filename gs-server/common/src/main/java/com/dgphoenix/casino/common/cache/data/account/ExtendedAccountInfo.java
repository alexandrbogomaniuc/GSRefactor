package com.abs.casino.common.cache.data.account;

public class ExtendedAccountInfo {

    private static final String REALITY_CHECK_INTERVAL = "REALITY_CHECK_INTERVAL";
    private static final String SELF_EXCLUSION_INTERVAL = "SELF_EXCLUSION_INTERVAL";
    private static final String SELF_EXCLUSION_END_DATE = "SELF_EXCLUSION_END_DATE";

    private ExtendedAccountInfo() {
    }

    public static String getRealityCheckIntervalKey() {
        return REALITY_CHECK_INTERVAL;
    }

    public static String getSelfExclusionIntervalKey() {
        return SELF_EXCLUSION_INTERVAL;
    }

    public static String getSelfExclusionEndDateKey() {
        return SELF_EXCLUSION_END_DATE;
    }
}

