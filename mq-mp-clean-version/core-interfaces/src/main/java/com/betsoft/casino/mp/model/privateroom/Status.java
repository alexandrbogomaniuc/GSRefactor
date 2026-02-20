package com.betsoft.casino.mp.model.privateroom;

public enum Status {
    INVITED,
    ACCEPTED,
    REJECTED,
    KICKED,
    LOADING,
    READY,
    WAITING,
    PLAYING;

    public static Status fromStringToStatus(final String value) {
        return Status.valueOf(value.toUpperCase());
    }

    public boolean canTransitionWithLimitsTo(Status nextStatus) {
        switch (this) {
            case INVITED:
                return nextStatus == INVITED || nextStatus == ACCEPTED || nextStatus == REJECTED;
            case ACCEPTED:
                return nextStatus == ACCEPTED;
            case REJECTED:
                return nextStatus == REJECTED;
            case WAITING:
                return nextStatus == WAITING;
            case KICKED:
                return nextStatus == KICKED || nextStatus == INVITED;
            default:
                return false;
        }
    }

    // Define transitions between states
    public boolean canTransitionTo(Status nextStatus) {
        switch (this) {
            /*case INVITED:
                return nextStatus == INVITED || nextStatus == ACCEPTED || nextStatus == REJECTED;
            case ACCEPTED:
                return nextStatus == ACCEPTED || nextStatus == LOADING;
            case REJECTED:
                return nextStatus == REJECTED;
            case LOADING:
                return nextStatus == LOADING || nextStatus == ACCEPTED || nextStatus == WAITING || nextStatus == READY;
            case WAITING:
                return  nextStatus == WAITING || nextStatus == READY || nextStatus == KICKED;
            case READY:
                return nextStatus == READY || nextStatus == PLAYING || nextStatus == WAITING
                || nextStatus == LOADING || nextStatus == KICKED;
            case PLAYING:
                return nextStatus == PLAYING || nextStatus == ACCEPTED || nextStatus == WAITING || nextStatus == READY;*/
            case KICKED:
                return nextStatus == KICKED || nextStatus == INVITED || nextStatus == WAITING;
            default:
                return true;
        }
    }
}
