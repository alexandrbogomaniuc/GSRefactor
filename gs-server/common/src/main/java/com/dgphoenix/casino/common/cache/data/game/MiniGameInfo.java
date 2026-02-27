package com.abs.casino.common.cache.data.game;

/**
 * @author <a href="mailto:zomac@dgphoenix.com">Roman Sorokin</a>
 * @since 1/26/23
 */
public class MiniGameInfo {

    private final long gameId;
    private long originalGameId;
    private String title;
    private String name;

    public MiniGameInfo(long gameId, long originalGameId, String title, String name) {
        this.gameId = gameId;
        this.originalGameId = originalGameId;
        this.title = title;
        this.name = name;
    }

    public MiniGameInfo(long gameId) {
        this.gameId = gameId;
    }

    public long getGameId() {
        return gameId;
    }

    public long getOriginalGameId() {
        return originalGameId;
    }

    public void setOriginalGameId(long originalGameId) {
        this.originalGameId = originalGameId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MiniGameInfo{" +
                "gameId=" + gameId +
                ", originalGameId=" + originalGameId +
                ", title='" + title + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}
