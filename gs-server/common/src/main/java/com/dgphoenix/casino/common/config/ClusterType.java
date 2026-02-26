package com.abs.casino.common.config;

import com.dgphoenix.casino.common.util.string.StringUtils;

public enum ClusterType {
    PRODUCTION("production"),
    STAGING("staging"),
    DEVELOPMENT("development");

    private final String stringRepresentation;

    ClusterType(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    public static ClusterType fromString(String text) {
        if(StringUtils.isTrimmedEmpty(text)) {
            throw new IllegalArgumentException("Param text is null or empty");
        }

        for (ClusterType clusterType : ClusterType.values()) {
            if (clusterType.stringRepresentation.equalsIgnoreCase(text)) {
                return clusterType;
            }
        }

        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
