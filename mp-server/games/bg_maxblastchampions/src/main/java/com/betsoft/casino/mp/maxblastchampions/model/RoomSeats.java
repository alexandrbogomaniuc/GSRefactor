package com.betsoft.casino.mp.maxblastchampions.model;

import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

public class RoomSeats implements Iterable<Seat>, Serializable {
    private static final Logger LOG = LogManager.getLogger(RoomSeats.class);
    private Seat[] seats;
    private final int numberOfSeats;
    private final long roomId;

    public RoomSeats(int numberOfSeats, long roomId) {
        this.numberOfSeats = numberOfSeats;
        this.roomId = roomId;
        this.seats = new Seat[numberOfSeats];
    }

    @Override
    public Iterator<Seat> iterator() {
        return Arrays.asList(getSeats()).iterator();
    }

    public Seat[] getSeats() {
        return seats;
    }

    public Seat get(int number) {
        return getSeats()[number];
    }

    public int getTotalSeatNumber() {
        return getSeats().length;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void addSeat(Seat seat, int number) throws CommonException {
        if (getSeats()[number] != null) {
            throw new CommonException("Seat is occupied");
        }
        getSeats()[number] = seat;
    }

    public void removeSeat(int seatNumber) {
        Seat seat = get(seatNumber);
        if (seat != null) {
            getSeats()[seatNumber] = null;
        }
    }

    public long getRoomId() {
        return roomId;
    }
}
