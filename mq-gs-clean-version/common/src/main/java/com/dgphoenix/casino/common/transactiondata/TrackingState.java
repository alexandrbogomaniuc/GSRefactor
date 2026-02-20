package com.dgphoenix.casino.common.transactiondata;

/**
 * Created by grien on 03.06.15.
 */
public class TrackingState {
    private int gameServerId;
    private TrackingStatus status;

    public TrackingState() {
    }

    public TrackingState(int gameServerId, TrackingStatus status) {
        this.gameServerId = gameServerId;
        this.status = status;
    }

    public int getGameServerId() {
        return gameServerId;
    }

    public void setGameServerId(int gameServerId) {
        this.gameServerId = gameServerId;
    }

    public TrackingStatus getStatus() {
        return status;
    }

    public void setStatus(TrackingStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackingState that = (TrackingState) o;

        if (gameServerId != that.gameServerId) return false;
        return status == that.status;
    }

    public boolean equals(int gameServerId, TrackingStatus status) {
        if (this.gameServerId != gameServerId) return false;
        return this.status == status;
    }

    @Override
    public int hashCode() {
        int result = gameServerId;
        result = 31 * result + status.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TrackingState [");
        sb.append("gameServerId=").append(gameServerId);
        sb.append(", status=").append(status);
        sb.append(']');
        return sb.toString();
    }
}
