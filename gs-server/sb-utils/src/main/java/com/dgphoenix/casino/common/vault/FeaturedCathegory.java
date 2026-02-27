package com.abs.casino.common.vault;

import java.util.List;

/**
 * Created by nkurtushin on 31.05.17.
 */
public class FeaturedCathegory extends AbstractCathegory{
    private final List<Long> games;

    public FeaturedCathegory(String title, List<Long> games) {
        this.games = games;
        this.title = title;
    }

    public List<Long> getGames() {
        return games;
    }
}
