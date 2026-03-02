package com.abs.casino.web;

import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.system.configuration.GameServerConfiguration;

public class VH {
    private static final String VERSION = "?version=";

    public static String v(String link) {
        GameServerConfiguration config = GameServerConfiguration.getInstance();
        if (!StringUtils.isTrimmedEmpty(config.getSourceVersion()) && config.isUseStaticVersioning())
            return link + VERSION + config.getSourceVersion();
        return link;
    }
}
