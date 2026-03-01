package com.abs.casino.common.cache;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;

import java.util.Set;

/**
 * User: flsh
 * Date: 12.01.15.
 */
public class SetOfLongsContainer implements IDistributedCacheEntry {
    private Set<Long> frbGames;

    public SetOfLongsContainer(Set<Long> frbGames) {
        this.frbGames = frbGames;
    }

    public SetOfLongsContainer() {
    }

    public Set<Long> getFrbGames() {
        return frbGames;
    }

    public void setFrbGames(Set<Long> frbGames) {
        this.frbGames = frbGames;
    }

    @Override
    public String toString() {
        return "SetOfLongsContainer[" +
                "frbGames=" + frbGames +
                ']';
    }
}
