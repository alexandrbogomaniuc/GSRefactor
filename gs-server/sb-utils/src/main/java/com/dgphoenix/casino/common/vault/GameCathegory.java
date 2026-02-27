package com.abs.casino.common.vault;

import java.util.List;

/**
 * Created by nkurtushin on 31.05.17.
 */
public class GameCathegory extends AbstractCathegory {
    private final List<GameEntry> games;

    public GameCathegory(String title, List<GameEntry> games) {
        this.title = title;
        this.games = games;
    }

    public List<GameEntry> getGames() {
        return games;
    }
}
