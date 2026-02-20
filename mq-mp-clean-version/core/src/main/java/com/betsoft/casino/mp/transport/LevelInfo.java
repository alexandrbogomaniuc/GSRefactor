package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ILevelInfo;

import java.io.Serializable;

public class LevelInfo implements ILevelInfo, Serializable {
    private int level;
    private long score;
    private long minScore;
    private long maxScore;

    public LevelInfo(int level, long score, long minScore, long maxScore) {
        this.level = level;
        this.score = score;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static LevelInfo convert(ILevelInfo level) {
        return level instanceof LevelInfo ? (LevelInfo) level : new LevelInfo(level.getLevel(), level.getScore(),
                level.getMinScore(), level.getMaxScore());
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public long getScore() {
        return score;
    }

    @Override
    public long getMinScore() {
        return minScore;
    }

    @Override
    public long getMaxScore() {
        return maxScore;
    }

    @Override
    public String toString() {
        return "LevelInfo{" +
                "level=" + level +
                ", score=" + score +
                ", minScore=" + minScore +
                ", maxScore=" + maxScore +
                '}';
    }
}
