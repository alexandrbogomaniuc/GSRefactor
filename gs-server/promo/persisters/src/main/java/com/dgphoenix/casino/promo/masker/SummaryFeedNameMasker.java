package com.abs.casino.promo.masker;

import com.dgphoenix.casino.common.util.string.StringUtils;

import java.util.List;

public abstract class SummaryFeedNameMasker<T> {
    private static final int SHORT_NAME_LENGTH = 5;
    private static final int LONG_NAME_MASK_LENGTH = 3;
    private static final int SHORT_NAME_MASK_LENGTH = 1;

    public abstract List<T> getFeedEntriesWithMaskedNames();

    protected String maskName(String originalName) {
        if (StringUtils.isTrimmedEmpty(originalName)) {
            return originalName;
        }
        String nameWithoutEmail = cutOffEmail(originalName);
        String mask = determineMask(nameWithoutEmail);
        return maskName(nameWithoutEmail, mask);
    }

    private String cutOffEmail(String name) {
        String nameWithoutEmail = name;
        if (name.contains("@")) {
            nameWithoutEmail = name.substring(0, name.indexOf('@')).trim();
        }
        return nameWithoutEmail;
    }

    private String determineMask(String name) {
        int repeatTimes = name.length() > SHORT_NAME_LENGTH ? LONG_NAME_MASK_LENGTH : SHORT_NAME_MASK_LENGTH;
        return org.apache.commons.lang3.StringUtils.repeat("*", repeatTimes);
    }

    private String maskName(String name, String mask) {
        String maskedName;
        if (name.length() > 1) {
            maskedName = name.substring(0, name.length() - mask.length()) + mask;
        } else {
            maskedName = name + mask;
        }
        return maskedName;
    }
}
