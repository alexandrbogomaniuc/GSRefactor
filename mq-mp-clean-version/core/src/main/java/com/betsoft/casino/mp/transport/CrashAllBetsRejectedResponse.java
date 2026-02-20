package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ICrashAllBetsRejected;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;
import java.util.StringJoiner;

public class CrashAllBetsRejectedResponse extends TObject implements ICrashAllBetsRejected {
    private int seatId;
    private  String name;
    private Long balance;

    public CrashAllBetsRejectedResponse(long date, int rid, int seatId, String name) {
        super(date, rid);
        this.seatId = seatId;
        this.name = name;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CrashAllBetsRejectedResponse that = (CrashAllBetsRejectedResponse) o;

        if (seatId != that.seatId) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + seatId;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashAllBetsRejectedResponse.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("seatId=" + seatId)
                .add("name='" + name + "'")
                .toString();
    }
}
