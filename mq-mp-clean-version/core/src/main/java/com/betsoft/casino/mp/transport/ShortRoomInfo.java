package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 28.08.18.
 */
public class ShortRoomInfo extends TObject {
    private long id;
    private short seats;
    private RoomState state;

    public ShortRoomInfo(long date, int rid, long id, short seats, RoomState state) {
        super(date, rid);
        this.id = id;
        this.seats = seats;
        this.state = state;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public short getSeats() {
        return seats;
    }

    public void setSeats(short seats) {
        this.seats = seats;
    }

    public RoomState getState() {
        return state;
    }

    public void setState(RoomState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ShortRoomInfo [");
        sb.append("id=").append(id);
        sb.append(", seats=").append(seats);
        sb.append(", state=").append(state);
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
