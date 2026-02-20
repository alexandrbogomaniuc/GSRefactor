package com.betsoft.casino.bots.mqb;

/**
 * User: flsh
 * Date: 13.07.2022.
 */
public enum BotStatuses {
    OK(0, "Success"),
    NOT_FOUND(1, "Bot not found"),
    WAITING_FOR_NEW_ROUND(2, "Waiting"),
    PLAYING(3, "Playing"),
    OBSERVING(4, "Observing")
    ;

    BotStatuses(int statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }

    private int statusCode;
    private String description;

    public int getStatusCode() {
        return statusCode;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BotStatuses [");
        sb.append("statusCode=").append(statusCode);
        sb.append(", description='").append(description).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
