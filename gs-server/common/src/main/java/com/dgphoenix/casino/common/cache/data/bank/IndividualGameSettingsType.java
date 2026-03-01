package com.abs.casino.common.cache.data.bank;

import com.abs.casino.common.util.string.StringUtils;

/**
 * User: flsh
 * Date: 11.07.13
 */
public enum IndividualGameSettingsType {
    PLAYER(true), FBLEVEL(true), NONE(false);
    private boolean hasIndividualSettings;

    private IndividualGameSettingsType(boolean hasIndividualSettings) {
        this.hasIndividualSettings = hasIndividualSettings;
    }

    public boolean isHasIndividualSettings() {
        return hasIndividualSettings;
    }

    public static IndividualGameSettingsType getByName(String name) {
        if (!StringUtils.isTrimmedEmpty(name)) {
            try {
                return IndividualGameSettingsType.valueOf(name);
            } catch (Exception ignore) {
            }
        }
        return NONE;
    }
}
