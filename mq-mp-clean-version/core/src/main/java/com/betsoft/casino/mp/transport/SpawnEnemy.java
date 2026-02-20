package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

/**
 * TODO: remove after debug
 */
public class SpawnEnemy extends TObject {
    private int typeId;
    private int skinId;
    private long roomId;
    private boolean fixTime;
    private String trajectory;
    private int gameId;

    public SpawnEnemy(long date, int rid, int typeId, int skinId, long roomId, boolean fixTime, String trajectory, int gameId) {
        super(date, rid);
        this.typeId = typeId;
        this.skinId = skinId;
        this.roomId = roomId;
        this.fixTime = fixTime;
        this.trajectory = trajectory;
        this.gameId = gameId;
    }

    public int getTypeId() {
        return typeId;
    }

    public int getSkinId() {
        return skinId;
    }

    public long getRoomId() {
        return roomId;
    }

    public String getTrajectory() {
        return trajectory;
    }

    public boolean isFixTime() {
        return fixTime;
    }

    public int getGameId() {
        return gameId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpawnEnemy [");
        sb.append("typeId=").append(typeId);
        sb.append(", skinId=").append(skinId);
        sb.append(", roomId=").append(roomId);
        sb.append(", fixTime=").append(fixTime);
        sb.append(", trajectory='").append(trajectory).append('\'');
        sb.append(", gameId=").append(gameId);
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
