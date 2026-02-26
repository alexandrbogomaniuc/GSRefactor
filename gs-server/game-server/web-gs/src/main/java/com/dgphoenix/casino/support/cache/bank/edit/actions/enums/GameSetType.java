package com.abs.casino.support.cache.bank.edit.actions.enums;

import com.dgphoenix.casino.common.util.string.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: isirbis
 * Date: 13.09.14
 */
public enum GameSetType {
    ALL("all"),
    NOTMOBILE("notMobile"),
    MOBILE("mobile");

    private static final Map<String, GameSetType> byTypeMap = new HashMap<>();
    private final String name;

    static {
        for (GameSetType gameSetType : values()) {
            byTypeMap.put(gameSetType.getName().toLowerCase(), gameSetType);
        }
    }

    GameSetType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static GameSetType toGameSetType(String name) {
        return StringUtils.isTrimmedEmpty(name) ? null : byTypeMap.get(name.toLowerCase());
    }
}
