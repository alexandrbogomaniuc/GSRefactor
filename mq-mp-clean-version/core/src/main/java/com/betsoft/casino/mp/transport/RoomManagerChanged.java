package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IRoomManagerChanged;
import com.betsoft.casino.utils.TObject;

import java.util.Objects;

public class RoomManagerChanged extends TObject implements IRoomManagerChanged {
    private int roomManager;

    public RoomManagerChanged(long date, int rid, int roomManager) {
        super(date, rid);
        this.roomManager = roomManager;
    }

    public int getRoomManager() {
        return roomManager;
    }

    @Override
    public String toString() {
        return "RoomManagerChanged{" +
                "roomManager=" + roomManager +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RoomManagerChanged that = (RoomManagerChanged) o;
        return roomManager == that.roomManager;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roomManager);
    }
}
