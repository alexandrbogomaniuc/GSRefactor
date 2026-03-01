package com.abs.casino.common.cache.data.game;

import com.abs.casino.common.exception.CommonException;

import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 06.04.2009
 */
public enum GameGroup {
    SLOTS("Slots", 1),
    TABLE("Table", 2),
    KENO("Keno", 3),
    VIDEOPOKER("Video Poker", 4),
    SOFT_GAMES("Soft Games", 5),
    PYRAMID_POKER("Pyramid Poker", 6),
    SOFT_GAME_ARCADE("Arcade Soft Game", 7),
    MULTIHAND_POKER("Multihand Poker", 8),
    MULTISTACK_POKER("Multistack Poker", 8),
    RUSH_THE_ROYAL("Rush The Royal", 4),
    LIVE("Live Dealer Games", 13),
    ACTION_GAMES("Action Games", 14);

    private String groupName;
    private int groupId;

    private static final Map<String, GameGroup> byNameMap = new HashMap<String, GameGroup>(11);

    static {
        GameGroup[] values = GameGroup.values();
        for (GameGroup group : values) {
            byNameMap.put(group.getGroupName().toLowerCase(), group);
        }
    }

    GameGroup(String name, int groupId) {
        this.groupName = name;
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getGroupId() {
        return groupId;
    }

    public static GameGroup get(String name) throws CommonException {
        GameGroup group = byNameMap.get(name.toLowerCase());
        if (group != null) {
            return group;
        }
        throw new CommonException("not such a type:" + name);
    }


}
