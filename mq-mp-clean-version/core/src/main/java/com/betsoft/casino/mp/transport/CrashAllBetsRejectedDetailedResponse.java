package com.betsoft.casino.mp.transport;

import java.util.Objects;
import java.util.StringJoiner;

public class CrashAllBetsRejectedDetailedResponse extends CrashAllBetsRejectedResponse{
    private int errorCode;
    private String errorMessage;


    public CrashAllBetsRejectedDetailedResponse(long date, int rid, int seatId, String name, int errorCode, String errorMessage) {
        super(date, rid, seatId, name);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CrashAllBetsRejectedDetailedResponse that = (CrashAllBetsRejectedDetailedResponse) o;

        if (errorCode != that.errorCode) return false;
        return Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + errorCode;
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashAllBetsRejectedResponse.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("errorCode='" + errorCode + "'")
                .add("errorMessage='" + errorMessage + "'")
                .toString();
    }
}
