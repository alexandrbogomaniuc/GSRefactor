package com.abs.casino.entities.lobby;

import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.util.string.StringUtils;

/**
 * User: Grien
 * Date: 13.10.2011 16:06
 */
public class StLobbyMode {
    private String name;
    private GameMode gameMode;
    private Long bonusId;
    private String key;

    public StLobbyMode(GameMode gameMode, Long bonusId) {
        this(createName(gameMode, bonusId), gameMode, bonusId);
    }

    public StLobbyMode(String name, GameMode gameMode, Long bonusId) {
        this.name = name;
        this.gameMode = gameMode;
        this.bonusId = bonusId;
        this.key = createKey(gameMode, bonusId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ModeValue");
        sb.append("[name='").append(name).append('\'');
        sb.append(", gameMode=").append(gameMode);
        sb.append(", bonusId=").append(bonusId);
        sb.append(", key='").append(key).append('\'');
        sb.append(']');
        return sb.toString();
    }

    private static final String key_delim = "_";

    public static StLobbyMode convertKey(String key) {
        String[] values;
        if (StringUtils.isTrimmedEmpty(key) || (values = key.toUpperCase().split(key_delim)).length == 0) {
            return null;
        }
        try {
            GameMode mode = GameMode.valueOf(values[0]);
            Long bonusId = null;
            if (GameMode.BONUS == mode) {
                if (values.length < 2) {
                    return null;
                }
                bonusId = Long.valueOf(values[1]);
            }
            return new StLobbyMode(mode, bonusId);
        } catch (Throwable th) {
            //ThreadLog.error(ModeValue.class.getSimpleName() + ":convertKey key=" + key, th);
            return null;
        }
    }

    private static String createKey(GameMode mode, Long bonusId) {
        if (mode == null) {
            return null;
        }
        String result = mode.name();
        return (mode == GameMode.BONUS ? (result + key_delim + bonusId) : result).toLowerCase();
    }

    private static String createName(GameMode gameMode, Long bonusId) {
        return gameMode.getMoneyType() + (GameMode.BONUS == gameMode ? ("#" + bonusId) : "");
    }
}