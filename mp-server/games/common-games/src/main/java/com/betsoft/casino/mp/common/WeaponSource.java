package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;

import java.util.*;

public enum WeaponSource {
    ENEMY("Enemy", Arrays.asList(GameType.REVENGE_OF_RA.getGameId(), GameType.PIRATES.getGameId(), GameType.AMAZON.getGameId())),
    TREASURE("Treasure", Arrays.asList(GameType.REVENGE_OF_RA.getGameId(), GameType.PIRATES.getGameId(), GameType.AMAZON.getGameId())),
    LOOT_BOX("LootBox", Arrays.asList(GameType.REVENGE_OF_RA.getGameId(), GameType.PIRATES.getGameId(), GameType.AMAZON.getGameId())),
    QUEST("Quest", Arrays.asList(GameType.REVENGE_OF_RA.getGameId(), GameType.PIRATES.getGameId(), GameType.AMAZON.getGameId())),
    STONE("Stone", Collections.EMPTY_LIST),
    DROP_ON_SHOOT("Drop on Shot", Collections.singletonList(GameType.CLASH_OF_THE_GODS.getGameId())),
    KILL_AWARD("Kill Award", Collections.singletonList(GameType.CLASH_OF_THE_GODS.getGameId()));

    String title;
    List<Long> gameIds;

    WeaponSource(String title, List<Long> gameIds) {
        this.title = title;
        this.gameIds = gameIds;
    }

    public String getTitle() {
        return title;
    }

    public static WeaponSource getSourceByTitle(String title) {
        Optional<WeaponSource> first = Arrays.stream(values()).filter(source -> source.title.equals(title)).findFirst();
        return first.orElse(null);
    }

}
